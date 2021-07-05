package application;

import java.net.URL;
import java.util.*;
import java.io.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class FXMLcontroller implements Initializable {
	private MediaPlayer mediaPlayer;
	private Media media;
	@FXML
	private MediaView mediaView;
	@FXML 
	private Slider volumeSlider;
	@FXML
	private Label volumePercentage;
	@FXML
	private Slider progressSlider;
	@FXML
	private Label timePassed;
	@FXML
	private Label subtitleText;

	private String filePath;
	private LinkedHashMap<long[], String> subtitles;
	
	@FXML
	private void handleButtonAction(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a file (*.mp4), (*.mp3)", "*.mp4", "*.mp3");
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(null);
		filePath = file.toURI().toString();
		if (filePath != null) {
			media = new Media(filePath);
			mediaPlayer = new MediaPlayer(media);
			mediaView.setMediaPlayer(mediaPlayer);
			
			DoubleProperty width = mediaView.fitWidthProperty();
			DoubleProperty height = mediaView.fitHeightProperty();
			DoubleProperty paneHeight = new SimpleDoubleProperty(mediaView.getScene().getHeight() - 60);
			width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
			//height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
			height.bind(paneHeight);
			volumeSlider.setValue(mediaPlayer.getVolume() * 100);
			volumePercentage.setText(Integer.toString((int)Math.round(volumeSlider.getValue())) + "%");
			volumeSlider.valueProperty().addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					mediaPlayer.setVolume(volumeSlider.getValue() / 100);
					volumePercentage.setText(Integer.toString((int)Math.round(volumeSlider.getValue())) + "%");
				}
			});
			mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
				@Override
				public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
						Duration newValue) {
					progressSlider.setMax(media.getDuration().toSeconds());
					progressSlider.setValue(newValue.toSeconds());	
					int seconds = (int)(newValue.toSeconds() % 60);
					int minutes = (int)(newValue.toMinutes() % 60);
					int hours = (int)(newValue.toHours());
					String sec = Integer.toString(seconds), min = Integer.toString(minutes), ho = Integer.toString(hours);
					if (seconds < 10)
						sec = "0" + seconds;
					if (minutes < 10)
						min = "0" + minutes;
					if (hours < 10)
						ho = "0" + hours;
					timePassed.setText(ho + " : " + min + " : " + sec);
					if (subtitles != null) {
						/*Map.Entry<long[], String> s = subtitles.entrySet().iterator().next();
						long startTimeInMillis = s.getKey()[0];
						long endTimeInMillis = s.getKey()[1];
						if (newValue.toMillis() >= startTimeInMillis && newValue.toMillis() <= endTimeInMillis) {
							subtitleText.setVisible(true);
							subtitleText.toFront();
							subtitleText.setText(s.getValue());
						}
						if (newValue.toMillis() >= endTimeInMillis) {
							subtitleText.setVisible(false);
							subtitles.remove(s.getKey());
						}*/
						for (Map.Entry<long[], String> map : subtitles.entrySet()) {
							long startTimeInMillis = map.getKey()[0];
							long endTimeInMillis = map.getKey()[1];
					        String text = map.getValue();
							if (newValue.toMillis() <= startTimeInMillis)
								subtitleText.setVisible(false);
							if (newValue.toMillis() >= startTimeInMillis && newValue.toMillis() <= endTimeInMillis) {
								subtitleText.setVisible(true);
								subtitleText.toFront();
								subtitleText.setText(text);
								Font normal = Font.font("Arial", FontPosture.REGULAR, 35.0);
								subtitleText.setFont(normal);
								if (text.startsWith("<i>")) {
									Font italic = Font.font("Arial", FontPosture.ITALIC, 35.0);
									subtitleText.setFont(italic);
									String noTags = text.substring(3, text.length() - 4);
									subtitleText.setText(noTags);
								}
								break;
							}
						}
					}
				}				
			});
			progressSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
				}
			});
			
			mediaPlayer.play();
		}
	}
	
	@FXML 
	private void pauseVideo(ActionEvent event) {
		mediaPlayer.pause();
	}
	
	@FXML 
	private void playVideo(ActionEvent event) {
		mediaPlayer.play();
		mediaPlayer.setRate(1);
	}
	
	@FXML 
	private void stopVideo(ActionEvent event) {
		mediaPlayer.stop();
	}
	
	@FXML 
	private void fastVideo(ActionEvent event) {
		mediaPlayer.setRate(1.5);
	}
	
	@FXML 
	private void fasterVideo(ActionEvent event) {
		mediaPlayer.setRate(4);
	}
	
	@FXML 
	private void slowVideo(ActionEvent event) {
		mediaPlayer.setRate(.75);
	}
	
	@FXML 
	private void slowerVideo(ActionEvent event) {
		mediaPlayer.setRate(.5);
	}
	
	@FXML 
	private void AddSubtitles(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a file (*.srt)", "*.srt");
		fileChooser.getExtensionFilters().add(filter);
		File subtitleFile = fileChooser.showOpenDialog(null);
		subtitles = new LinkedHashMap<>();
		List<long[]> times = new ArrayList<>();
		int timeIndex = 0;
		String sText = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(subtitleFile)));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.matches("\\d+")) { //regex to check whether the line is an integer (//d+ checks for one or more digits)
					timeIndex = Integer.parseInt(line) - 1;
					continue;
				}
				if (line.contains("-->")) { //This line indicates time
					String[] arr = line.split(" ");
					String[] startTime = arr[0].split(":|,");
					String[] endTime = arr[2].split(":|,");
					long startHours = Long.parseLong(startTime[0]) * 3600 * 1000;
					long startMins = Long.parseLong(startTime[1]) * 60 * 1000;
					long startSecs = Long.parseLong(startTime[2]) * 1000;
					long startMillis = Long.parseLong(startTime[3]);
					long startTimeinMillis = startHours + startMins + startSecs + startMillis;
					long endHours = Long.parseLong(endTime[0]) * 3600 * 1000;
					long endMins = Long.parseLong(endTime[1]) * 60 * 1000;
					long endSecs = Long.parseLong(endTime[2]) * 1000;
					long endMillis = Long.parseLong(endTime[3]);
					long endTimeinMillis = endHours + endMins + endSecs + endMillis;
					long[] period = {startTimeinMillis, endTimeinMillis};
					times.add(period);
				} else if (!line.trim().equals("")) { // This line shows text to be displayed on screen
					if (sText.equals("")) 
						sText = line;
					else 
						sText += "\n" + line;
				} else {
					if (sText.equals(""))
						continue;
					subtitles.put(times.get(timeIndex), sText);
					sText = "";
				}
			}
			for (Map.Entry<long[], String> s : subtitles.entrySet()) {
				System.out.println(s.getKey()[0] + " " + s.getKey()[1] + " / " + s.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
			subtitleText.setText(e.getMessage());
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}

}
