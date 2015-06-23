package observer_daemon;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Observer extends Thread {
	private String seriale;
	private String telefono;
	private int sms;
	private int allarme;
	private String guidatore;
	private String targa;
	private GarminParser gp;

	public Observer(ResultSet rs) throws SQLException {
		rs.next();
		this.seriale = rs.getString("serial");
		this.telefono = rs.getString("telefono");
		sms = rs.getInt("sms");
		if (sms != 30)
			sms *= 2;
		sms *= 30;
		this.allarme = rs.getInt("velocita");
		this.guidatore = rs.getString("guidatore");
		this.targa = rs.getString("targa");
		this.gp = null;
	}

	public void setTrace(String file) throws Exception {
		this.gp = new GarminParser(file);
	}

	@Override
	public void run() {
		long previous1; // ins. storico
		long previous2; // sms
		// long previous_3; // aggiornamento
		long offset = (System.currentTimeMillis() / 1000L - gp.getTime());
		previous1 = System.currentTimeMillis() / 1000L;
		previous2 = System.currentTimeMillis() / 1000L;
		while (true) {
			long current = System.currentTimeMillis() / 1000L;
			if (current > gp.getTime() + offset)
				try {
					gp.next();
					System.out.println("next");
				} catch (Exception e) {
					e.printStackTrace();
				}
			if (current - previous1 > 300L) { // 5min
				inviaPosizione();
				aggiornaDati();
				System.out.println("argh");
				previous1 = current;
			}
			if (gp.getVelocita() > allarme) {
				inviaAllarme();
			}
			if (sms != 0 && current - previous2 > sms) {
				inviaSMS();
				previous2 = current;
			}
		}

	}

	private void inviaAllarme() {
		System.out.println("*************************\n" + "Inviata Email\n"
				+ "Inviato SMS\nTesto messaggi: " + "\nSeriale: " + seriale
				+ "\nVelocità  superata!\n\tlimite: " + allarme + "\n\tVeloci"
				+ "tà  rivlevata: " + gp.getVelocita());
	}

	private void inviaSMS() {
		System.out.println("*************************\n" + "Inviata Email\n"
				+ "Inviato SMS\nTesto messaggi: " + "\nSeriale: " + seriale
				+ "\nPosizione: " + gp.getPosizione());
	}

	private void aggiornaDati() {
		try {
			ResultSet rs = DataSource.getDati(seriale);
			rs.next();
			this.telefono = rs.getString("telefono");
			sms = rs.getInt("sms");
			if (sms != 30)
				sms *= 2;
			sms *= 30;
			this.allarme = rs.getInt("velocita");
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	private void inviaPosizione() {
		String query = "INSERT INTO storico(observer, usr, targa, velocita, posizione, istante) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try {
			MioDriver.getInstance()
					.update(query,
							new Object[] {
									seriale,
									guidatore,
									targa,
									gp.getVelocita(),
									gp.getPosizione(),
									new java.sql.Timestamp(System
											.currentTimeMillis()) });
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		Observer o = new Observer(DataSource.getDati("55555eeeee"));
		o.setTrace("examples/VERONA SOLFERINO.gpx.xml");
		o.run();
	}
}
