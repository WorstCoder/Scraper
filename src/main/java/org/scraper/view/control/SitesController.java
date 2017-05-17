package org.scraper.view.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import scraper.SitesUtility;
import scraper.data.Site;
import scraper.scraper.ScrapeType;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SitesController implements ISaveable, ILoader {
	@FXML
	private Button scrapeButton;
	
	@FXML
	private Button checkButton;
	
	@FXML
	private TextField addField;
	
	@FXML
	private Button addSite;
	
	@FXML
	private Button gatherButton;
	
	@FXML
	private TextField depthField;
	
	@FXML
	private TextField avgProxies;
	
	@FXML
	private TextField avgWorkingProxies;
	
	private SitesUtility model;
	
	private ISelectable<Site> selected;
	
	@FXML
	public void initialize(SitesUtility model, ISelectable<Site> selected) {
		
		this.model = model;
		
		this.selected = selected;
		
		scrapeButton.setOnAction(event -> scrape());
		
		checkButton.setOnAction(event -> check());
		
		gatherButton.setOnAction(event -> gather());
		
		addSite.setOnAction(event -> addSites());
		
		depthField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("[0-9]*"))
				depthField.setText(oldValue);
		});
		
		avgProxies.setOnAction(event ->
									 model.filterAvgProxies(parseToInt(avgProxies.getText())));
		
		avgWorkingProxies.setOnAction(event ->
									   model.filterAvgWorking(parseToInt(avgWorkingProxies.getText())));
		
	}
	
	public void addSites() {
		String sitesString = addField.getText();
		addField.clear();
		
		String[] sitesArr = sitesString.contains(" ")
				? sitesString.split(" ")
				: sitesString.contains("\n")
				? sitesString.split("\\n")
				: new String[]{sitesString};
		
		Arrays.stream(sitesArr).forEach(siteString -> model.addSite(siteString));
	}
	
	private void scrape() {
		model.scrape(selected.getSelected());
	}
	
	private void check() {
		model.assgn(selected.getSelected());
	}
	
	private void gather() {
		model.gather(selected.getSelected(), Integer.parseInt(depthField.getText()));
	}
	
	private Integer parseToInt(String toParse) {
		return toParse.equals("") ? 0 : Integer.parseInt(toParse);
	}
	
	@Override
	public void save(File file) {
		List<String> selectedAsStrings = selected.getSelected().stream()
				.map(Site::getAddress)
				.collect(Collectors.toList());
		
		saveFile(selectedAsStrings, file);
	}
	
	@Override
	public void load(File file) {
		List<String> loaded = readFile(file);
		
		loaded.forEach(string ->
							   model.addSite(new Site(string, ScrapeType.UNCHECKED)));
	}
	
}