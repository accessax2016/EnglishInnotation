package application;

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import model.Subtitle;
import model.Video;

public class MainController {

	@FXML
	private MediaView mediaVideo;
	@FXML
	private Label lblMediaVideo;
	@FXML
	private HBox mediaTool;
	@FXML
	private Label lblTitle;
	@FXML
	private TextField txtSearchSubtitle;
	@FXML
	private ListView<Subtitle> listSubtitle;
	@FXML
	private Button btnAddVideo;
	@FXML
	private Button btnDeleteVideo;
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
	private TabPane tab;
	
	// Unity
	private MediaPlayer mediaPlayer;
	private Duration duration;
	private List<Video> videos = new ArrayList<Video>();
	private int indexSelectedVideo;
	private List<Subtitle> subtitles = new ArrayList<Subtitle>();
	private int indexSelectedSubtitle;
	ObservableList<Subtitle> observableList = FXCollections.observableArrayList();
	private boolean notEvent = false;
	private boolean playing = false;
	
	
	public void initialize() {
        System.out.println("initilize");
        
        lblMediaVideo.setDisable(true);
        tab.getSelectionModel().select(1);
		btnAddSubtitle.setDisable(true);
		btnDeleteVideo.setDisable(true);
		btnPlay.setDisable(true);
		sliderTimeVideo.setDisable(true);
		indexSelectedVideo = -1;
		indexSelectedSubtitle = 0;
        
		setListVideo();
		
        setActionListVideo();
        setActionSliderTimeVideo();
        setActionListSubtitle();
        setActionTextboxSearchSubtitle();
        
    }

	public void setListVideo() {
		
		readListVideo();
		for (int i = 0; i < videos.size(); i++) {
			listVideo.getItems().add(videos.get(i).getName());
		}
		
	}
	
	public void setActionListVideo() {
		listVideo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (newValue == null) {
					// reset state
					btnPlay.setText("►");
					sliderTimeVideo.setValue(0);
					listSubtitle.getItems().clear();
					subtitles.clear();
					
					lblTitle.setText("Không có Video");
					btnDeleteVideo.setDisable(true);
					btnAddSubtitle.setDisable(true);
					return;
				}
				
				lblMediaVideo.setText("");;
				lblMediaVideo.setDisable(false);
				btnDeleteVideo.setDisable(false);
				btnPlay.setDisable(false);
				sliderTimeVideo.setDisable(false);
				indexSelectedVideo = listVideo.getSelectionModel().getSelectedIndex();
				
				lblTitle.setText(videos.get(indexSelectedVideo).getName());
				
				setMediaVideo(videos.get(indexSelectedVideo).getUrlVideo());
				
				if (videos.get(indexSelectedVideo).getUrlSubtitle() != null) {
					readFileSubtitle(videos.get(indexSelectedVideo).getUrlSubtitle());
					
					observableList.setAll(subtitles);
					listSubtitle.setItems(observableList);
					listSubtitle.setCellFactory(param -> new Cell());
					
				}
				
				btnAddSubtitle.setDisable(false);
				tab.getSelectionModel().select(0);
				
				mediaPlayer.play();
			}
		});
	}
	
	public void setActionListSubtitle() {
		listSubtitle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Subtitle>() {

			@Override
			public void changed(ObservableValue<? extends Subtitle> observable, Subtitle oldValue, Subtitle newValue) {
				// TODO Auto-generated method stub
				if (notEvent) return;
				if (newValue == null) return;
				
				SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = null;
                try {
                    date = sdf.parse(newValue.getStartTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mediaPlayer.seek(Duration.millis(date.getTime()));
                indexSelectedSubtitle = listSubtitle.getSelectionModel().getSelectedIndex() + 1;
			}
		});
	}
	
	public void handleButtonPlay() {
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
        	btnPlay.setText("►");
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
        btnPlay.setText("►");
		sliderTimeVideo.setValue(0);
		listSubtitle.getItems().clear();
		// action
		play();
	}
	
	public void handleLabelMediaVideo() {
		if (playing) {
			mediaPlayer.pause();
		}
		else {
			mediaPlayer.play();
		}
	}
	
	public void setActionTextboxSearchSubtitle() {
		txtSearchSubtitle.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				observableList.clear();
				for (Subtitle subtitle : subtitles) {
					if (subtitle.getContent().toLowerCase().contains(newValue.toLowerCase())) {
						observableList.add(subtitle);
					}
				}
			}
		});
	}
	
	public void setActionSliderTimeVideo() {
		sliderTimeVideo.valueChangingProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				// TODO Auto-generated method stub

				mediaPlayer.seek(duration.multiply(sliderTimeVideo.getValue() / 100.0));
				updateTimeVideo();
				
				
			}
		});
		sliderTimeVideo.valueChangingProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean wasChanging, Boolean chaning) {
				// TODO Auto-generated method stub
				if (chaning) {
					mediaPlayer.pause();
				}
				
				if (wasChanging) {
					changeSelectSubtitle(0);
					notEvent = true;
					listSubtitle.getSelectionModel().select(indexSelectedSubtitle - 1);
					listSubtitle.scrollTo(indexSelectedSubtitle - 2  < 0 ? 0 : indexSelectedSubtitle - 2 );
					notEvent = false;
					mediaPlayer.play();
					
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
		mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {

			@Override
			public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
				// TODO Auto-generated method stub
				updateTimeVideo();
            	changeSelectSubtitle(0);
            	notEvent = true;
				listSubtitle.getSelectionModel().select(indexSelectedSubtitle - 1);
//				listSubtitle.scrollTo(indexSelectedSubtitle - 1);
				notEvent = false;
				
			}
		});
		mediaPlayer.setOnPlaying(new Runnable() {
            public void run() {
            	btnPlay.setText("||");
            	playing = true;
            }
        });

		mediaPlayer.setOnPaused(new Runnable() {
            public void run() {
            	btnPlay.setText("►");
                playing = false;
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
            	indexSelectedSubtitle = 0;
            	mediaPlayer.pause();
            }
        });
    }
	
	public void changeSelectSubtitle(int index) {
		Duration currentTime = mediaPlayer.getCurrentTime();
		
		int i = index;
		while (i < subtitles.size()) {
			if (currentTime.greaterThan(convertStringToDuration(subtitles.get(i).getStartTime()))) {
				i++;
			}
			else {
				indexSelectedSubtitle = i;
				return;
			}
		}
		indexSelectedSubtitle = subtitles.size();
		

	}
	
	public Duration convertStringToDuration(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        Date date = null;
		try {
			date = sdf.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Duration.millis(date.getTime());
	}
	
	public void readFileSubtitle(String url){ 
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(url)))) {
        	subtitles.clear();
            String line;
            while ((line = reader.readLine()) != null){   
                String startTime = line.substring(0, 5);
                String content = line.substring(5).trim();
                subtitles.add(new Subtitle(startTime, content, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public File locateFile(Node node) {
		FileChooser chooser = new FileChooser();
	    chooser.setTitle("Chon Video");
	    return chooser.showOpenDialog(node.getScene().getWindow());
	
	}
	
	public void addVideo(ActionEvent event) {
		Node node = (Node) event.getSource();
		File file = locateFile(node); 
		if (file != null) {
			videos.add(new Video(file.getName(), file.toURI().toString()));
			
			listVideo.getItems().add(file.getName());
			
			listVideo.getSelectionModel().selectLast();
		}
	
	}
	
	public void addSubtitle(ActionEvent event) {
		if (indexSelectedVideo > -1) {
			Node node = (Node) event.getSource();
			File file = locateFile(node);
			if (file != null) {
				listSubtitle.getItems().clear();
						
				readFileSubtitle(file.toString());
				
				observableList.setAll(subtitles);
				listSubtitle.setItems(observableList);
				listSubtitle.setCellFactory(param -> new Cell());
				
				videos.get(indexSelectedVideo).setUrlSubtitle(file.toString());
			}
		}

	}
	
	public void deleteVideo(ActionEvent event) {
		if (indexSelectedVideo > -1) {
			videos.remove(indexSelectedVideo);
			listVideo.getItems().remove(indexSelectedVideo);
		}
	}
	
	public void writeListVideo() {
		try {
			File file = new File(".\\videos.txt");
			if (file.exists()) {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream fout = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);

			oos.writeObject(videos);

			oos.close();
			fout.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}	
	}
	
	@SuppressWarnings("unchecked")
	public void readListVideo() {
		try {
			FileInputStream fin = new FileInputStream(new File(".\\videos.txt"));
			ObjectInputStream ois = new ObjectInputStream(fin);
			
			videos = (ArrayList<Video>)ois.readObject(); 
				
			ois.close();
			fin.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public void writeListSubtitle() {
//		try {
//			File file = new File(".\\subtitles.txt");
//			if (file.exists()) {
//				file.delete();
//				try {
//					file.createNewFile();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			FileOutputStream fout = new FileOutputStream(file);
//			ObjectOutputStream oos = new ObjectOutputStream(fout);
//
//			oos.writeObject(subtitles);
//
//			oos.close();
//			fout.close();
//		} catch (FileNotFoundException e) {
//			System.out.println("File not found");
//		} catch (IOException e) {
//			System.out.println("Error initializing stream");
//		}	
//	}
//	
//	@SuppressWarnings("unchecked")
//	public void readListSubtitle() {
//		try {
//			FileInputStream fin = new FileInputStream(new File(".\\subtitles.txt"));
//			ObjectInputStream ois = new ObjectInputStream(fin);
//			
//			subtitles = (ArrayList<Subtitle>)ois.readObject(); 
//				
//			ois.close();
//			fin.close();
//		} catch (FileNotFoundException e) {
//			System.out.println("File not found");
//		} catch (IOException e) {
//			System.out.println("Error initializing stream");
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public void stop() {
		writeListVideo();
//		writeListSubtitle();
		System.out.println("stop");
	}

}
