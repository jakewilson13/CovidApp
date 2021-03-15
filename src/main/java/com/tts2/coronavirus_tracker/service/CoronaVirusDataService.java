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
    public void fetchVirusData () throws IOException, InterruptedException {
    	//creating the list in the case if someone access the server while it's updating they will still get an output of the latest recorded update
    	List<LocationStats> newStats = new ArrayList<>();
    	
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        		//uri is saying where do we need to make an http request
                .uri(URI.create(VIRUS_DATA_URL))
                .build();   							//taking the body and returning it as a String
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        // ^ What's happening is we are making a call to the virus data url and retreving all of the data and returning it as a String.
        
        
        //String reader reads character streams as a String
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        
        //Iterable allows an object to be the target of a for-loop
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        // ^ parsing all of the data with the open source library for apache commons csv
        
        

        for (CSVRecord record : records) {
        	LocationStats locationStat = new LocationStats();
        	
        	locationStat.setState(record.get("Province/State"));
        	locationStat.setCountry(record.get("Country/Region"));
        	
        	//specifying a number to get a column
        	int latestCases = Integer.parseInt(record.get(record.size() - 1)); /*getting the latest reported cases referring to array index*/
        	int prevDayCases = Integer.parseInt(record.get(record.size() - 2)); /*getting the cases from the previous day, 2nd to last size*/
        	locationStat.setLatestTotalCases(latestCases);
        	locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
        	newStats.add(locationStat);
        	
        }
        this.allStats = newStats;
    }

}
