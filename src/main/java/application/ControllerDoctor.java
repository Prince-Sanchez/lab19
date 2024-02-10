package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import view.DoctorView;

/*
 * Controller class for doctor registration and profile update.
 */
@Controller
public class ControllerDoctor {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Request for new doctor registration form.
	 */
	@GetMapping("/doctor/register")
	public String getNewDoctorForm(Model model) {
		// return blank form for new patient registration
		model.addAttribute("doctor", new DoctorView());
		return "doctor_register";
	}
	
	/*
	 * Process doctor registration.
	 */
	@PostMapping("/doctor/register")
	public String createDoctor(DoctorView doctor, Model model) {
		
		try (Connection con = getConnection();) {
			PreparedStatement ps = con.prepareStatement("insert into doctor(last_name, first_name, specialty, practice_since,  ssn ) values(?, ?, ?, ?, ?)", 
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, doctor.getLast_name());
			ps.setString(2, doctor.getFirst_name());
			ps.setString(3, doctor.getSpecialty());
			ps.setString(4, doctor.getPractice_since_year());
			ps.setString(5, doctor.getSsn());
			
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) doctor.setId(rs.getInt(1));
		
			// display message and patient information
			model.addAttribute("message", "Registration successful.");
			model.addAttribute("doctor", doctor);
			return "doctor_show";
			
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_register";	
		}
	}
	
	/*
	 * Request blank form for doctor search.
	 */
	@GetMapping("/doctor/get")
	public String getSearchForm(Model model) {
		// return form to enter doctor id and name
		model.addAttribute("doctor", new DoctorView());
		return "doctor_get";
	}
	
	/*
	 * Search for doctor by id and name.
	 */
	@PostMapping("/doctor/get")
	public String showDoctor(DoctorView doctor, Model model) {
		
		System.out.println("showDoctor "+doctor);  // debug
		
		try (Connection con = getConnection();) {
			
			PreparedStatement ps = con.prepareStatement("select last_name, first_name, specialty, practice_since from doctor where id=? and last_name=?");
			ps.setInt(1, doctor.getId());
			ps.setString(2, doctor.getLast_name());
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doctor.setLast_name(rs.getString(1));
				doctor.setFirst_name(rs.getString(2));
				doctor.setPractice_since_year(rs.getString(4));
				doctor.setSpecialty(rs.getString(3));
				model.addAttribute("doctor", doctor);
				System.out.println("end getDoctor "+doctor);  // debug 
				return "doctor_show";
				
			} else {
				model.addAttribute("message", "Doctor not found.");
				model.addAttribute("doctor", doctor);
				return "doctor_get";
			}
						
		} catch (SQLException e) {
			System.out.println("SQL error in getDoctor "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_get";
		}
	}
	
	/*
	 * search for doctor by id.
	 */
	@GetMapping("/doctor/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {
		
		System.out.println("getUpdateForm "+id);
		
		DoctorView doctor = new DoctorView();
		doctor.setId(id);
		try (Connection con = getConnection();) {

			PreparedStatement ps = con.prepareStatement("select last_name, first_name, specialty, practice_since from doctor where id=?");
			ps.setInt(1,  id);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doctor.setLast_name(rs.getString(1));
				doctor.setFirst_name(rs.getString(2));
				doctor.setPractice_since_year(rs.getString(4));
				doctor.setSpecialty(rs.getString(3));
				model.addAttribute("doctor", doctor);
				return "doctor_edit";
			} else {
				model.addAttribute("message", "Doctor not found.");
				model.addAttribute("doctor", doctor);
				return "doctor_get";
			}
			
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_get";
			
		}
	}
	
	/*
	 * process profile update for doctor. Only change specialty or year of practice.
	 */
	@PostMapping("/doctor/edit")
	public String updateDoctor(DoctorView doctor, Model model) {
		System.out.println("updateDoctor " + doctor);
		try (Connection con = getConnection();) {

			PreparedStatement ps = con.prepareStatement("update doctor set specialty=?, practice_since=? where id=?");
			ps.setString(1, doctor.getSpecialty());
			ps.setString(2, doctor.getPractice_since_year());
			ps.setInt(3, doctor.getId());

			int rc = ps.executeUpdate();

			// rc is row count from executeUpdate
			// should be 1

			if (rc == 1) {
				model.addAttribute("message", "Update successful");
				model.addAttribute("doctor", doctor);
				return "doctor_show";

			} else {
				model.addAttribute("message", "Error. Update was not successful");
				model.addAttribute("doctor", doctor);
				return "doctor_edit";
			}

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error." + e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_edit";
		}
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
