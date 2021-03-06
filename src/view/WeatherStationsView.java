package view;

import controller.StationsFilterController;
import interfaces.IJsonSerializable;
import interfaces.IWeatherSystemCallback;
import interfaces.WeatherSystem;
import model.State;
import model.WeatherStation;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherStationsView extends JPanel implements IWeatherSystemCallback, IJsonSerializable {
	private static final long serialVersionUID = 1L;

	WeatherSystem system;

	JScrollPane scrollPane;
	JPanel panel = new JPanel();

	JPanel filterPanel = new JPanel();
	JComboBox<State> stateFilter = new JComboBox<State>(State.values());
	JTextField nameFilter = new JTextField();

	List<StationEntryView> entryList = new ArrayList<StationEntryView>();

	JButton buttonFavourite = new JButton("Favourite");

	public WeatherStationsView(WeatherSystem system) {
		this.system = system;
		system.registerRefreshableCallback(this);
		initializeWindow();
		Refresh();
	}

	/**
	 * Initialises the window/frame.
	 */
	private void initializeWindow() {
		// Setup scroll pane related data
		scrollPane = new JScrollPane(panel);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// Create filter section
		TitledBorder border = BorderFactory.createTitledBorder("Weather Station Filter");
		border.setTitleJustification(TitledBorder.CENTER);
		filterPanel.setBorder(border);
		filterPanel.setLayout(new GridLayout(1, 2, 10, 10));
		filterPanel.add(stateFilter);
		filterPanel.add(nameFilter);
		this.add(filterPanel);

		StationsFilterController filterController = new StationsFilterController(system);
		stateFilter.addActionListener(filterController);
		nameFilter.addActionListener(filterController);

		// Setup panel layouts and add to main panel
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(scrollPane);
	}

	@Override
	public void Refresh() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				List<WeatherStation> filteredStations = system.getWeatherStations((State) stateFilter.getSelectedItem(),
						nameFilter.getText());

				// Clear entries
				for (StationEntryView s : entryList) {
					panel.remove(s);
				}

				entryList.clear();

				for (WeatherStation station : filteredStations) {
					StationEntryView stationEntry = new StationEntryView(station, system);
					panel.add(stationEntry);
					entryList.add(stationEntry);
				}
				scrollPane.validate();
				scrollPane.repaint();
			}

		});
	}

	@Override
	public JSONObject SaveToJsonObject() {
		JSONObject object = new JSONObject();

		object.put("windowPosX", this.getX());
		object.put("windowPosY", this.getY());

		object.put("windowWidth", this.getWidth());
		object.put("windowHeight", this.getHeight());

		// TODO any other values to save?

		return object;
	}

	@Override
	public void LoadFromJsonObject(JSONObject obj) {
		this.setBounds(obj.getInt("windowPosX"), obj.getInt("windowPosY"), obj.getInt("windowWidth"),
				obj.getInt("windowHeight"));
	}
}
