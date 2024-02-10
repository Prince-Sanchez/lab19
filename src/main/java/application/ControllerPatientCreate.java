package application;

import java.sql.Connection;
import java.sql.SQLException;

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
	public String createPatient(PatientView p, Model model) {

		/*
		 * validate doctor last name and find the doctor id
		 */
		// TODO

		/*
		 * insert to patient table
		 */


		// display patient data and the generated patient ID,  and success message
		model.addAttribute("message", "Registration successful.");
		model.addAttribute("patient", p);
		return "patient_show";

		/*
		 * on error
		 * model.addAttribute("message", some error message);
		 * model.addAttribute("patient", p);
		 * return "patient_register";
		 */
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

		// TODO   search for patient by id and name
		
		// if found, return "patient_show", else return error message and "patient_get"
		
		return "patient_show";
	}
	
	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}
}
