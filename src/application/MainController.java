package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class MainController {

	@FXML
	private MediaView mediaVideo;
	@FXML
	private HBox mediaTool;
	@FXML
	private Label lblTitle;
	@FXML
	private TextField txtSearchSubtitle;
	@FXML
	private CheckBox chkFavorite;
	@FXML
	private ListView<String> listSubtitle;
	@FXML
	private Button btnAddVideo;
	@FXML
	private Button btnAddSubtitle;
	@FXML
	private ListView<String> listVideo;
	@FXML
	private Button btnPlay;
	@FXML
	private Slider sliderTimeVideo;
	@FXML
	private Label lblTimeVideo;
	@FXML
	private Slider sliderVloume;
	
	// Unity
//	private Media media;
	private MediaPlayer mediaPlayer;
	private Duration duration;
	
	
	public void initialize() {
        System.out.println("initilize");
        
        setListVideo();
        setActionListVideo();
        
        
//        setMediaVideo("E:\\Dev Sofware\\Java\\code\\EnglishInnotation\\resources\\videos\\video1.mp4");
//        setActionSliderTimeVideo();
//        
//        readFileSubtitle("E:\\Dev Sofware\\Java\\code\\EnglishInnotation\\resources\\subtitles\\video1.txt");
//        setActionListSubtitle();
        
//        play();
        
    }

	public void setListVideo() {
		ObservableList<String> items = FXCollections.observableArrayList ("video1", "video2", "video3", "video4");
        listVideo.setItems(items);
	}
	
	public void setActionListVideo() {
		listVideo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				mediaPlayer.stop();
				btnPlay.setText(">");
				sliderTimeVideo.setValue(0);
				
				listSubtitle.getItems().clear();
				
				setMediaVideo("/videos/" + newValue + ".mp4");
				
				readFileSubtitle("E:\\Dev Sofware\\Java\\code\\EnglishInnotation\\resources\\subtitles\\" + newValue + ".txt");
				
				play();
			}
		});
	}
	
	public void setActionListSubtitle() {
		listSubtitle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (newValue == null) return;
				
				SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = null;
                try {
                    date = sdf.parse(newValue.substring(0,5));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mediaPlayer.seek(Duration.millis(date.getTime()));
			}
		});
	}
	
	public void handleButtonPlay(ActionEvent e) {
        MediaPlayer.Status status = mediaPlayer.getStatus();

        if (status == MediaPlayer.Status.UNKNOWN  || status == MediaPlayer.Status.HALTED)
        {
            // don't do anything in these states
            return;
        }

        if ( status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
            mediaPlayer.play();
            btnPlay.setText("||");
        } 
        else {
        	mediaPlayer.pause();
        	btnPlay.setText(">");
        }
    }
	
	public void setMediaVideo(String uri) {
		if (uri == null)
            return;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        
		Media media = new Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaVideo.setMediaPlayer(mediaPlayer);
        mediaVideo.setPreserveRatio(true);
        // reset state
        btnPlay.setText(">");
		sliderTimeVideo.setValue(0);
		listSubtitle.getItems().clear();
	}
	
	public void setActionSliderTimeVideo() {
		sliderTimeVideo.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (sliderTimeVideo.isValueChanging()) {
                    // multiply duration by percentage calculated by slider position
                    mediaPlayer.seek(duration.multiply(sliderTimeVideo.getValue() / 100.0));
                }
            }
        });
		
	}
	
	public void updateTimeVideo() {
		Platform.runLater(new Runnable() {
            @SuppressWarnings("deprecation")
			public void run() {
                Duration currentTime = mediaPlayer.getCurrentTime();
                lblTimeVideo.setText(formatTime(currentTime, duration));
                sliderTimeVideo.setDisable(duration.isUnknown());
                if (!sliderTimeVideo.isDisabled() && duration.greaterThan(Duration.ZERO) && !sliderTimeVideo.isValueChanging()) {
                	sliderTimeVideo.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }
            }
        });
    }
	
	public String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int)Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int)Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 -
                    durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
	
	public void play(){
		mediaPlayer.currentTimeProperty().addListener(new InvalidationListener()
        {
            public void invalidated(Observable ov) {
            	updateTimeVideo();
            }
        });
		mediaPlayer.setOnPlaying(new Runnable() {
            public void run() {
            	btnPlay.setText("||");
            }
        });

		mediaPlayer.setOnPaused(new Runnable() {
            public void run() {
                btnPlay.setText(">");
            }
        });
		mediaPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mediaPlayer.getMedia().getDuration();
                updateTimeVideo();
            }
        });		
		mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
            	mediaPlayer.seek(mediaPlayer.getStartTime());
            	sliderTimeVideo.setValue(0);
            	mediaPlayer.pause();
            }
        });
    }
	
	public void readFileSubtitle(String url){
	        try (BufferedReader reader = new BufferedReader(new FileReader(new File(url)))) {
	            String line;
	            while ((line = reader.readLine()) != null){
//	            	System.out.println(line);
	                listSubtitle.getItems().add(line);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	
	public void locateFile(ActionEvent event) {
		Node node = (Node) event.getSource();
		
	    FileChooser chooser = new FileChooser();
	    chooser.setTitle("Chon Video");
	    File file = chooser.showOpenDialog(node.getScene().getWindow());
	    if(file != null){

			
			
			setMediaVideo(file.toURI().toString());
			
			play();
	    }   
	    
	    
		
		
		
	}
}
