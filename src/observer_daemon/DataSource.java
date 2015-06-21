package observer_daemon;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataSource {
	
	public static ResultSet getDati(String serial) throws SQLException{
		MioDriver d = MioDriver.getInstance();
		ResultSet res;
		
		String query = "SELECT o.serial, o.sms, a.velocita, u.telefono, u.email, uv.email AS guidatore, v.targa "
				+ "FROM observer o "
				+ "JOIN veicolo_observer vo ON o.serial = vo.serial "
				+ "JOIN veicolo v ON vo.targa = v.targa "
				+ "JOIN usr u ON v.gestore = u.email "
				+ "JOIN allarme a ON o.serial = a.observer "
				+ "JOIN usr_veicolo uv ON v.targa = uv.targa "
				+ "WHERE o.serial = '" + serial + "' " 
				+ "AND uv.fine IS NULL "
				+ "AND vo.fine IS NULL";
        res = d.execute(query, null);
        return res;
	}

}

