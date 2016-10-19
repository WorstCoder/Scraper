package org.scraper.control;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.scraper.model.modles.MainModel;

public class BarController {
	@FXML
	private Button scrapeButton;
	@FXML
	private Button checkButton;
	@FXML
	private Button gatherButton;
	@FXML
	private TextField depthField;
	@FXML
	private ToggleButton checkOnFly;
	@FXML
	private Label uiLog;
	
	private MainModel model;
	
	@FXML
	public void initialize(MainModel model) {
		
		this.model = model;
		
		scrapeButton.setOnAction(event -> model.scrape());
		
		checkButton.setOnAction(event -> model.check());
		
		gatherButton.setOnAction(event -> model.crawl());
		
		checkButton.setOnAction(event -> model.setCheckOnFly(checkButton.isArmed()));
	}
	
}
