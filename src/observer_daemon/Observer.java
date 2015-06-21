package observer_daemon;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Observer extends Thread {
	private String seriale;
	private String telefono;
	private int sms;
	private String posizione;
	private int allarme;
	private int velocita;
	private String guidatore;
	private String targa;

	public Observer(String seriale, String telefono, int sms, int allarme) {
		super();
		this.seriale = seriale;
		this.telefono = telefono;
		this.sms = sms;
		this.allarme = allarme;
		this.posizione = "39.19371,-77.00447";
		this.velocita = 0;
		
	}
	public Observer() {
		super();
		this.seriale = "1q2w3e4r5t6y7u";
		this.telefono = "030918692";
		this.sms = 10000;
		this.allarme = 90;
		this.posizione = "39.19371,-77.00447";
		this.velocita = 0;
		
	}

	public Observer(ResultSet rs) throws SQLException {
		rs.next();
		this.seriale = rs.getString("serial");
		this.telefono = rs.getString("telefono");
		sms = rs.getInt("sms");
		if (sms != 30)
			sms *= 2;
		sms *= 30 * 1000;
		this.allarme = rs.getInt("velocita");
		this.guidatore = rs.getString("guidatore");
		this.targa = rs.getString("targa");
		this.posizione = "39.19371,-77.00447";
		this.velocita = 0;
	}

	@Override
	public void run() {
		long previous1; // ins. storico
		long previous2; // sms
		// long previous_3; // aggiornamento
		previous1 = System.currentTimeMillis();
		previous2 = System.currentTimeMillis();
		while (true) {
			long current = System.currentTimeMillis();
			aggiornaVelocita();

			if (current - previous1 > 300000L / 100) {
				inviaPosizione();
				aggiornaDati();
				System.out.println("argh");
				previous1 = current;
			}
			if (velocita > allarme) {
				//inviaAllarme();
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
				+ "tà  rivlevata: " + velocita);
	}

	private void inviaSMS() {
		System.out.println("*************************\n" + "Inviata Email\n"
				+ "Inviato SMS\nTesto messaggi: " + "\nSeriale: " + seriale
				+ "\nPosizione: " + posizione);
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
			MioDriver.getInstance().update(query, new Object[] {
					seriale, guidatore, targa, velocita, posizione, new java.sql.Timestamp(System.currentTimeMillis() / 1000L)
			});
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	private void aggiornaVelocita() {
		velocita = (short) Math.floor((Math.random() * 200) + 1);
	}	
	
	
	public static void main(String[] args) throws SQLException {
		Observer o = new Observer(DataSource.getDati("55555eeeee"));
		o.run();
	}
}
