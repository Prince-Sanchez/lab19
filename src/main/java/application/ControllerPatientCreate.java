package application;

import java.sql.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatientCreate {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	/*
	 * Request blank patient registration form.
	 */
	@GetMapping("/patient/new")
	public String getNewPatientForm(Model model) {
		// return blank form for new patient registration
		model.addAttribute("patient", new PatientView());
		return "patient_register";
	}

	/*
	 * Process data from the patient_register form
	 */
	@PostMapping("/patient/new")
	public String createPatient(PatientView p, Model model) throws SQLException {

		// TODO
		String docLast = p.getPrimaryName(); //validate the last name
		int docID;
		try (Connection con = getConnection();){ //find the doctor id
			PreparedStatement ps = con.prepareStatement("select id from doctor where last_name =?");
			ps.setString(1, docLast);

			ResultSet rs = ps.executeQuery();
			if (rs.next()){
				docID = rs.getInt(1); // docID = doctor ID obtained
			}
			else {
				model.addAttribute("message", "Doctor not found."); // error message if no doctor ID found
			}
		}

		/*
		 * insert to patient table
		 */
		try (Connection con = getConnection();){
			PreparedStatement ps = con.prepareStatement("insert into patient (last_name, first_name, " +
							"birthdate, ssn, street, city, state, zip, primaryName) values(?,?,?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, p.getLast_name());
			ps.setString(2, p.getFirst_name());
			ps.setString(3, p.getBirthdate());
			ps.setString(4, p.getSsn());
			ps.setString(5, p.getStreet());
			ps.setString(6, p.getCity());
			ps.setString(7, p.getState());
			ps.setString(8, p.getZipcode());
			ps.setString(9, p.getPrimaryName());

			ps.executeUpdate(); // insert into patient table
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) p.setId(rs.getInt(1)); // sets patient ID

			// display patient data and the generated patient ID,  and success message
			model.addAttribute("message", "Registration successful.");
			model.addAttribute("patient", p);
			return "patient_show";
		} catch (SQLException e) { // on error
			model.addAttribute("message", "Patient could not be registered because of " + e);
			model.addAttribute("patient", p);
			return "patient_register";
		}

	}

	/*
	 * Request blank form to search for patient by id and name
	 */
	@GetMapping("/patient/edit")
	public String getSearchForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_get";
	}

	/*
	 * Perform search for patient by patient id and name.
	 */
	@PostMapping("/patient/show")
	public String showPatient(PatientView p, Model model) {

		String lastName = p.getLast_name();
		int patientId = p.getId();
		try (Connection con = getConnection();){ //find patient w last name and id
			PreparedStatement ps = con.prepareStatement("select first_name, street, city," +
					" state, zip, birthdate, primaryName from patient where last_name =? and patient_id=?"); // search using id and last name
			ps.setString(1, lastName);
			ps.setInt(2, patientId);

			ResultSet rs = ps.executeQuery(); // retrieve patient info
			if (rs.next()){
				p.setFirst_name(rs.getString(1));
				p.setStreet(rs.getString(2));
				p.setCity(rs.getString(3));
				p.setState(rs.getString(4));
				p.setZipcode(rs.getString(5));
				p.setBirthdate(rs.getString(6));
				p.setPrimaryName(rs.getString(7));
				model.addAttribute("patient", p);
				return "patient_show";
			}
			else {
				model.addAttribute("message", "Patient not found.");
				model.addAttribute("patient", p);// error message if patient not found
				return "patient_get";
			}
		} catch (SQLException e) {
			model.addAttribute("message","SQL error in getPatient "+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";
		}
		// if found, return "patient_show", else return error message and "patient_get"
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}
}
