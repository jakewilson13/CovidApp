package com.tts2.coronavirus_tracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.tts2.coronavirus_tracker.model.LocationStats;
import com.tts2.coronavirus_tracker.service.CoronaVirusDataService;


@Controller
public class HomeController {
	//autowiring our service in so we have access to those methods
	@Autowired
	CoronaVirusDataService coronaVirusDataService;
	
	@GetMapping(value = "/")
	//can hold model attributes when you want them in the html
	public String home (Model model) {
		List<LocationStats> allStats = coronaVirusDataService.getAllStats();
		//taking list of objects and converting it into a stream
		//then mapping it to an integer value which is the total cases for that record & then sums it up
		int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
		int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
		//we need to get the value allStats and put it in the model.addAttribute so it can display on the page
		model.addAttribute("locationStats", allStats);
		model.addAttribute("totalReportedCases", totalReportedCases);
		model.addAttribute("totalNewCases", totalNewCases);
		return "home";
	}
}
