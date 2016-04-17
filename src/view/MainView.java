package view;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import model.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import controller.MainViewController;

public class MainView extends JFrame implements IJsonSerializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String STATIONS_LABEL = "Weather Stations";
	public static final String FAVS_LABEL = "Favourite";
	public static final String REFRESH_LABEL = "Refresh";
	
	WeatherSystem system;
	WeatherStationsView weatherStationView; // WeatherStation View
	FavouritesView favoritesView;
	
	JPanel panel = new JPanel(new BorderLayout());
	JPanel topPanel = new JPanel();
	JPanel bottomPanel = new JPanel();
	JLabel weather = new JLabel();
	Font font = new Font("Calibri", Font.PLAIN, 15);

	JButton buttonWeather = new JButton(STATIONS_LABEL);
	JButton buttonFavourites = new JButton(FAVS_LABEL);
	JButton buttonRefresh = new JButton(REFRESH_LABEL);
	
	
	public MainView(WeatherSystem system)
	{	
		this.system = system;
		weatherStationView = new WeatherStationsView(system);
		favoritesView = new FavouritesView(system);
		InitializeWindow();
		AttachActionListeners();
	}
	
	/**
	 * Initialises the view of the window/frame.
	 */
	private void InitializeWindow()
	{
		bottomPanel.add(buttonWeather);
		bottomPanel.add(buttonFavourites);
		bottomPanel.add(buttonRefresh);
		
		buttonWeather.setFont(font);
		buttonFavourites.setFont(font);
		buttonRefresh.setFont(font);
		
		Border lineBorder = BorderFactory.createLineBorder(new Color(255,180,0));
		Border emptyBorder = new EmptyBorder(5,5,5,5);
		CompoundBorder border = new CompoundBorder(lineBorder, emptyBorder);
		buttonWeather.setBorder(border);
		buttonFavourites.setBorder(border);
		buttonRefresh.setBorder(border);
		
		buttonWeather.setBackground(new Color(227,227,227));
		buttonFavourites.setBackground(new Color(227,227,227));
		buttonRefresh.setBackground(new Color(227,227,227));

		bottomPanel.setBackground(new Color(219,238,254));
		bottomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		panel.setBackground(new Color(219,238,254));

		JLabel label = new JLabel("Welcome to the Weather App", SwingConstants.CENTER);
		label.setFont(new Font("Myriad Pro", Font.PLAIN, 21));
		topPanel.setBackground(new Color(219,238,254));
		topPanel.add(label);

		weather.setIcon(new ImageIcon("images/weather.png"));
		topPanel.add(weather);
		
		//There is a bug where you set the minSize and maxSize 
		//It will minSize the frame but not maxSize
		//http://bugs.java.com/bugdatabase/view_bug.do;?bug_id=6200438
		//so I set the resizable to false
		this.add(panel);
		this.setTitle("Weather App");
		this.setSize(400,400); //needs to be changed
		this.setLocationRelativeTo(null); //centre the frame - needs to be changed
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setMinimumSize(new Dimension(400,400));
		//this.setMaximumSize(new Dimension(450,500));
		this.setResizable(false);
		
		//Load the stub of sub windows
		loadProgramState();
		
		this.setVisible(true);
	}
	
	/**
	 * This function would ideally be removed
	 * we need to transition this window listener to controller.
	 * However we're reliant on functionality in this class. 
	 * TODO:
	 */
	private void AttachActionListeners()
	{
		MainViewController controller = new MainViewController(bottomPanel, system,
				weatherStationView, favoritesView);
		
		buttonWeather.addActionListener(controller);
		buttonFavourites.addActionListener(controller);
		buttonRefresh.addActionListener(controller);
		
		this.addWindowListener( new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				saveProgramState();
				System.exit(0);
			}
		});
	}
	
	private void loadProgramState()
	{
		String windowStatesJson = "";
		
		try 
		{
            FileReader reader = new FileReader("WindowStates.json");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = "";
            
            while((line = bufferedReader.readLine()) != null) 
            {
            	windowStatesJson += line;
            }   
            
            bufferedReader.close();
            
		}
		catch (IOException e) { return; }
		
		JSONTokener tokener = new JSONTokener(windowStatesJson);
		JSONArray stateArray = new JSONArray(tokener);
		
		//TODO for now we assume indexes until we can
		// find a way to save objects with window name.
		LoadFromJsonObject(stateArray.getJSONObject(0));
		weatherStationView.LoadFromJsonObject(stateArray.getJSONObject(1));
	}
	
	private void saveProgramState()
	{
		JSONArray windowArray = new JSONArray();
		windowArray.put(SaveToJsonObject());
    	windowArray.put(weatherStationView.SaveToJsonObject());
    	
    	try 
    	{
			PrintWriter writer = new PrintWriter("WindowStates.json");
			writer.print(windowArray.toString());
			writer.close();
		} 
    	catch (FileNotFoundException e1) { }
	}
	
	@Override
	public JSONObject SaveToJsonObject() 
	{
		JSONObject object = new JSONObject();
		
		object.put("windowPosX", this.getX());
		object.put("windowPosY", this.getY());
		
		object.put("windowWidth", this.getWidth());
		object.put("windowHeight", this.getHeight());
		
		//TODO any other values to save?
		
		return object;
	}

	@Override
	public void LoadFromJsonObject(JSONObject obj) 
	{
		this.setBounds(obj.getInt("windowPosX"), obj.getInt("windowPosY"),
				obj.getInt("windowWidth"), obj.getInt("windowHeight"));
	}
}
