package uniandes.unacloud.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.losandes.utils.Constants;

import unacloud.enums.UserRestrictionEnum;
import uniandes.unacloud.db.entities.User;
import db.DatabaseConnection;
import db.RepositoryManager;

/**
 * Generic class used to query and update User entity in database
 * @author Cesar
 *
 */
public class UserManager {
	
	/**
	 * Returns an User entity request by param id
	 * @param id from user
	 * @return User
	 */
	public static User getUser(Long id){
		try {
			Connection con = DatabaseConnection.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT u.id, u.username FROM user u WHERE u.id = ?;");
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();			
			if(rs.next())return new User(rs.getLong(1),rs.getString(2));
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns an user entity with allow repository
	 * @param id
	 * @return
	 */
	public static User getUserWithRepository(Long id){
		try {
			Connection con = DatabaseConnection.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT u.id, u.username, usr.val FROM user u LEFT JOIN (SELECT uur.user_restrictions_id as id, ur.value as val from user_user_restriction uur INNER JOIN user_restriction ur ON uur.user_restriction_id = ur.id WHERE ur.name = ? AND uur.user_restrictions_id = ?) usr ON u.id = usr.id WHERE u.id = ? ;");
			ps.setString(1, UserRestrictionEnum.REPOSITORY.name());
			ps.setLong(2, id);
			ps.setLong(3, id);
			ResultSet rs = ps.executeQuery();
			String repository = null;
			User user = null;
			if(rs.next()){
				user= new User(rs.getLong(1),rs.getString(2));
				repository = rs.getString(3);
			}
			user.setRepository(RepositoryManager.getRepositoryByName(repository==null?Constants.MAIN_REPOSITORY:repository));
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
