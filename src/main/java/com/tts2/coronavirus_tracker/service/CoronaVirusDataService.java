package com.tts2.coronavirus_tracker.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tts2.coronavirus_tracker.model.LocationStats;


@Service
public class CoronaVirusDataService {
//using a csv URL

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    //allows you to create instances of LocationStats model
    private List<LocationStats> allStats = new ArrayList<>();
    
    //created a getter for allSTats because it's private
    //now our controller can retrieve this data and display on the html
    public List<LocationStats>getAllStats() {
    	return allStats;
    }
    //postconstruct tells spring to run this method after the build
    @PostConstruct
    //tells spring to execute this method the first hour everyday using the cron time standard
    @Scheduled(cron = "* * 1 * * *")
  //will make an http call to the url
    //exceptions are there if the client request fails
    public void fetchVirusData () throws IOException, InterruptedException {
    	//creating the list in the case if someone access the server while it's updating they will still get an output of the latest recorded update
    	List<LocationStats> newStats = new ArrayList<>();
    	
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        
        for (CSVRecord record : records) {
        	LocationStats locationStat = new LocationStats();
        	locationStat.setState(record.get("Province/State"));
        	locationStat.setCountry(record.get("Country/Region"));
        	int latestCases = Integer.parseInt(record.get(record.size() - 1));
        	int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
        	locationStat.setLatestTotalCases(latestCases);
        	locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
        	newStats.add(locationStat);
        	
        }
        this.allStats = newStats;
        
        
    }

}
