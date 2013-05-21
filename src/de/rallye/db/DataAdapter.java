package de.rallye.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.rallye.exceptions.DataException;
import de.rallye.model.structures.Group;
import de.rallye.model.structures.Node;
import de.rallye.model.structures.PrimitiveEdge;

public class DataAdapter {
	
	private static Logger logger = LogManager.getLogger(DataAdapter.class.getName());
	
	private ComboPooledDataSource dataSource;

	
	public DataAdapter(ComboPooledDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * appends a list of string separated by ", "
	 * @param strings
	 * @return
	 */
	private static String strStr(String... strings) {
		StringBuilder b = new StringBuilder();
		
		int l = strings.length-1;
		for (int i=0; i<=l; i++) {
			b.append(strings[i]);
			if (i < l)
				b.append(", ");
		}
		
		return b.toString();
	}
	
	private static void close(Connection con, Statement st, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			if (con != null) {
				con.close();
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	
	public List<Group> getGroups() throws DataException {

		Statement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ strStr(Ry.Groups.ID, Ry.Groups.NAME, Ry.Groups.DESCRIPTION) +" FROM "+ Ry.Groups.TABLE);

			List<Group> groups = new ArrayList<Group>();
			
			while (rs.next()) {
				groups.add(new Group(rs.getInt(1), rs.getString(2), rs.getString(3)));
			}

			return groups;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	public List<Node> getNodes() throws DataException {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ strStr(Ry.Nodes.ID, Ry.Nodes.NAME, Ry.Nodes.LAT, Ry.Nodes.LON, Ry.Nodes.DESCRIPTION) +" FROM "+ Ry.Nodes.TABLE);

			ArrayList<Node> nodes = new ArrayList<Node>();
			
			while (rs.next()) {
				nodes.add(new Node(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getString(5)));
			}

			return nodes;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public List<PrimitiveEdge> getEdges() throws DataException {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ strStr(Ry.Edges.A, Ry.Edges.B, Ry.Edges.TYPE) +" FROM "+ Ry.Edges.TABLE);

			ArrayList<PrimitiveEdge> edges = new ArrayList<PrimitiveEdge>();
			
			while (rs.next()) {
				edges.add(new PrimitiveEdge(rs.getInt(1), rs.getInt(2), rs.getString(3)));
			}

			return edges;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
}
