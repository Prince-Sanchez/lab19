package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;
/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatientUpdate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {

		PatientView pv = new PatientView();
		// TODO search for patient by id
		//  if not found, return to home page using return "index"; 
		//  else create PatientView and add to model.
		// model.addAttribute("message", some message);
		// model.addAttribute("patient", pv
		// return editable form with patient data
		return "patient_edit";		 		
}
	
	
	/*
	 * Process changes from patient_edit form
	 *  Primary doctor, street, city, state, zip can be changed
	 *  ssn, patient id, name, birthdate, ssn are read only in template.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView p, Model model) {

		// validate doctor last name 
		
		// TODO 
		
		// TODO update patient profile data in database

		// model.addAttribute("message", some message);
		// model.addAttribute("patient", p)
		return "patient_show";
	}
	
	
}
