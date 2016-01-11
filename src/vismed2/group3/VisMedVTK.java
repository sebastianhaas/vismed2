package vismed2.group3;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import vismed2.group3.dicom.DicomExporter;
import vismed2.group3.filters.GradientFilter;
import vismed2.group3.filters.MIP;
import vismed2.group3.filters.MedianFilter;
import vismed2.group3.filters.ThresholdFilter;
import vismed2.group3.filters.VtkJavaFilter;
import vtk.vtkDICOMImageReader;
import vtk.vtkImageData;
import vtk.vtkNativeLibrary;

/**
 * This class is the application entry point of an university project done for
 * the visualization of medical data course in the winter term 2015. It launches
 * a window containing three image views and a control panel. The image views
 * are showing a monochrome example data set of a basin, retrieved by a CAT
 * scan.
 * 
 * <h4>Usage and Controls</h4> The top-left view shows a XY-, the top-right a
 * XZ- and the bottom-left view a YZ-oriented interpretation of the data. The
 * user is able to individually control brightness and contrast for each view by
 * dragging the mouse up and down respectively left and right. The zoom level
 * can be controlled using the mouse wheel. The sliders on the right can be used
 * to control what slice will be shown on the respective view.
 * 
 * <h4>Filters</h4> This application offers a set of filters that can be used to
 * alter the image data. The filters currently built in are:
 * <ul>
 * <li>Gradient Filter
 * <ul>
 * <li>Gradient XY</li>
 * <li>Roberts</li>
 * <li>Sobel</li>
 * </ul>
 * </li>
 * <li>Median</li>
 * <li>MIP</li>
 * <li>Thresholding</li>
 * </ul>
 * Filters can be applied by selecting the desired filter and clicking the
 * <i>Apply</i> button. Filters will be either applied to the entire image
 * volume, or to the actively displayed slices only. This behavior can be
 * controlled using the <i>Apply filter on...</i> combo box.<br>
 * <b>Note:</b> Some filters are only available in active-slice-mode due to
 * performance reasons.
 * 
 * <h4>DICOM export</h4> Clicking <i>Export DICOM</i> will start the export
 * process. The currently displayed image will be exported in DICOM format using
 * dummy patient information. Keep in mind that some filters will be applied on
 * the currently active slices only; therefore, the exported DICOM data might
 * not show the processed image on the default (first) slice. Data will be
 * written to data/output/. Make sure this directory exists and is writable.
 * 
 * <h4>Implementation details</h4> This class uses
 * {@link vismed2.group3.ImageViewerPanel} to draw VTK views onto a regular
 * lightweight Swing frame. Due to restrictions of VTK's Java-Wrappings, it is
 * not possible to access the actual native views until they are initialized and
 * drawn on the screen. Since the authors did not find an event triggered at
 * this specific state, a static 1500ms timer is used as a workaround.<br>
 * <br>
 * All filters as well as the DICOM export will run in separate non-blocking
 * worker threads to guarantee application responsiveness.
 * 
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 *
 */
public class VisMedVTK extends JPanel implements ChangeListener, ActionListener {
	private static final long serialVersionUID = 1L;
	private vtkDICOMImageReader dicomReader;
	private vtkImageData currentImageData;
	private vtkImageData currentImageData_backup;
	private ImageViewerPanel panel0;
	private ImageViewerPanel panel1;
	private ImageViewerPanel panel2;
	private JSlider sliceSlider0;
	private JSlider sliceSlider1;
	private JSlider sliceSlider2;
	private JLabel sliceSliderLabel0;
	private JLabel sliceSliderLabel1;
	private JLabel sliceSliderLabel2;
	private int currentSlice0 = 0;
	private int currentSlice1 = 0;
	private int currentSlice2 = 0;
	private JButton buttonApplyFilter;
	private JButton buttonExport;
	private static StatusBar statusBar;
	private ProgressMonitor progressMonitor;
	private boolean crosshairsFlag = false;
	private JComboBox comboBoxFilterSelector;
	private JComboBox comboBoxSliceSelector;
	String[] filterSelectorItemsAllSlices = { "Median", "Treshold" };
	String[] filterSelectorItemsActiveSlice = { "Gradient XY", "Median", "MIP", "Roberts", "Sobel", "Treshold" };
	String[] filterSliceSelectorItems = { "Active slice", "All slices" };

	/**
	 * Prepares the panel and sets up all VTK components. Afterwards a sample
	 * data set is loaded and displayed.
	 */
	public VisMedVTK() {
		super(new BorderLayout());

		// Get DICOM image data
		dicomReader = new vtkDICOMImageReader();
		File directory = new File("data/Bassin");
		dicomReader.SetDirectoryName(directory.getAbsolutePath());
		dicomReader.Update();
		currentImageData = dicomReader.GetOutput();

		currentImageData_backup = new vtkImageData();
		currentImageData_backup.Initialize();
		currentImageData_backup.CopyStructure(currentImageData);
		currentImageData_backup.CopyAttributes(currentImageData);
		currentImageData_backup.DeepCopy(currentImageData);

		panel0 = new ImageViewerPanel(currentImageData);
		panel1 = new ImageViewerPanel(currentImageData);
		panel2 = new ImageViewerPanel(currentImageData);

		JPanel content = new JPanel(new MigLayout("fill, gap 1, insets 0, wrap 2, debug"));

		// Prepare controls
		JPanel controlsPanel = new JPanel(new MigLayout());

		JPanel sliderPanel = new JPanel(new MigLayout("wrap 2, fillx"));
		sliderPanel.setBorder(BorderFactory.createTitledBorder("Slices"));
		sliceSlider0 = new JSlider(JSlider.HORIZONTAL, panel0.getSliceMin(), panel0.getSliceMax(), currentSlice0);
		sliceSlider0.addChangeListener(this);
		sliceSliderLabel0 = new JLabel(String.format("%d/%d", sliceSlider0.getValue(), sliceSlider0.getMaximum()));
		sliceSlider1 = new JSlider(JSlider.HORIZONTAL, panel1.getSliceMin(), panel1.getSliceMax(), currentSlice1);
		sliceSlider1.addChangeListener(this);
		sliceSliderLabel1 = new JLabel(String.format("%d/%d", sliceSlider1.getValue(), sliceSlider1.getMaximum()));
		sliceSlider2 = new JSlider(JSlider.HORIZONTAL, panel2.getSliceMin(), panel2.getSliceMax(), currentSlice2);
		sliceSlider2.addChangeListener(this);
		sliceSliderLabel2 = new JLabel(String.format("%d/%d", sliceSlider2.getValue(), sliceSlider2.getMaximum()));
		sliderPanel.add(sliceSlider0);
		sliderPanel.add(sliceSliderLabel0);
		sliderPanel.add(sliceSlider1);
		sliderPanel.add(sliceSliderLabel1);
		sliderPanel.add(sliceSlider2);
		sliderPanel.add(sliceSliderLabel2);

		JPanel filterPanel = new JPanel(new MigLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

		filterPanel.add(new JLabel("Apply filter on:"));
		comboBoxSliceSelector = new JComboBox(filterSliceSelectorItems);
		comboBoxSliceSelector.addActionListener(this);
		filterPanel.add(comboBoxSliceSelector, "wrap");

		filterPanel.add(new JLabel("Filter:"));
		comboBoxFilterSelector = new JComboBox(filterSelectorItemsActiveSlice);
		comboBoxFilterSelector.addActionListener(this);
		filterPanel.add(comboBoxFilterSelector, "");
		buttonApplyFilter = new JButton("Apply");
		buttonApplyFilter.addActionListener(this);
		filterPanel.add(buttonApplyFilter, "wrap");

		buttonExport = new JButton("Export as DICOM");
		buttonExport.addActionListener(this);
		filterPanel.add(buttonExport, "");

		controlsPanel.add(sliderPanel, "wrap");
		controlsPanel.add(filterPanel);

		content.add(panel0, "grow");
		content.add(panel1, "grow");
		content.add(panel2, "grow");
		content.add(controlsPanel, "");

		add(content, BorderLayout.CENTER);
		statusBar = new StatusBar();
		add(statusBar, BorderLayout.SOUTH);

		Runnable initViewsDelayRunnable = new Runnable() {
			public void run() {
				try {
					Thread.sleep(1500);
					panel0.setSliceOrientation(ImageViewerPanel.ORIENTATION_XY);
					panel1.setSliceOrientation(ImageViewerPanel.ORIENTATION_XZ);
					panel2.setSliceOrientation(ImageViewerPanel.ORIENTATION_YZ);
					panel0.render();
					panel1.render();
					panel2.render();

					// Set slider values since they depend on orientation
					sliceSlider0.setMinimum(panel0.getSliceMin());
					sliceSlider0.setMaximum(panel0.getSliceMax());
					currentSlice0 = panel0.getSlice();
					sliceSliderLabel0
							.setText(String.format("%d/%d", sliceSlider0.getValue(), sliceSlider0.getMaximum()));
					sliceSlider1.setMinimum(panel1.getSliceMin());
					sliceSlider1.setMaximum(panel1.getSliceMax());
					currentSlice1 = panel1.getSlice();
					sliceSliderLabel1
							.setText(String.format("%d/%d", sliceSlider1.getValue(), sliceSlider1.getMaximum()));
					sliceSlider2.setMinimum(panel2.getSliceMin());
					sliceSlider2.setMaximum(panel2.getSliceMax());
					currentSlice2 = panel2.getSlice();
					sliceSliderLabel2
							.setText(String.format("%d/%d", sliceSlider2.getValue(), sliceSlider2.getMaximum()));
				} catch (InterruptedException iex) {
				}
			}
		};
		Thread initViewsDelayThread = new Thread(initViewsDelayRunnable);
		initViewsDelayThread.start();
	}

	public static void main(String s[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("VisMed2 VTK Gruppe 3");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setLayout(new BorderLayout());
				frame.getContentPane().add(new VisMedVTK(), BorderLayout.CENTER);
				frame.setSize(700, 700);
				frame.setVisible(true);
			}
		});
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(sliceSlider0)) {
			currentSlice0 = sliceSlider0.getValue();
			if (crosshairsFlag) {
				panel0.setInputData(currentImageData_backup);
			}
			panel0.setSlice(currentSlice0);
			sliceSliderLabel0.setText(String.format("%d/%d", sliceSlider0.getValue(), sliceSlider0.getMaximum()));
		} else if (e.getSource().equals(sliceSlider1)) {
			currentSlice1 = sliceSlider1.getValue();
			if (crosshairsFlag) {
				panel1.setInputData(currentImageData_backup);
			}
			panel1.setSlice(currentSlice1);
			sliceSliderLabel1.setText(String.format("%d/%d", sliceSlider1.getValue(), sliceSlider1.getMaximum()));
		} else if (e.getSource().equals(sliceSlider2)) {
			currentSlice2 = sliceSlider2.getValue();
			if (crosshairsFlag) {
				panel2.setInputData(currentImageData_backup);
			}
			panel2.setSlice(currentSlice2);
			sliceSliderLabel2.setText(String.format("%d/%d", sliceSlider2.getValue(), sliceSlider2.getMaximum()));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonApplyFilter)) {
			if (comboBoxFilterSelector.getSelectedItem().equals("Gradient XY")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					GradientFilter gradient = new GradientFilter();
					gradient.setFilter(GradientFilter.Type.GradientXY);
					crosshairsFlag = gradient.setAllSlices(false);
					gradient.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(gradient);
				}
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Sobel")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					GradientFilter gradient = new GradientFilter();
					gradient.setFilter(GradientFilter.Type.Sobel);
					crosshairsFlag = gradient.setAllSlices(false);
					gradient.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(gradient);
				}
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Roberts")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					GradientFilter gradient = new GradientFilter();
					gradient.setFilter(GradientFilter.Type.Roberts);
					crosshairsFlag = gradient.setAllSlices(false);
					gradient.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(gradient);
				}
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Median")) {
				MedianFilter median = new MedianFilter();
				median.SetKernelSize(3, 3, 3);
				if (comboBoxSliceSelector.getSelectedItem().equals("All slices")) {
					crosshairsFlag = median.setAllSlices(true);
				} else {
					crosshairsFlag = median.setAllSlices(false);
				}
				median.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
				applyFilter(median);
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Treshold")) {
				ThresholdFilter threshold = new ThresholdFilter();
				if (comboBoxSliceSelector.getSelectedItem().equals("All slices")) {
					crosshairsFlag = threshold.setAllSlices(true);
				} else {
					crosshairsFlag = threshold.setAllSlices(false);
				}
				threshold.setUpperThreshold(1000.0);
				threshold.setLowerThreshold(500.0);
				threshold.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
				applyFilter(threshold);
			} else if (comboBoxFilterSelector.getSelectedItem().equals("MIP")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					MIP mip = new MIP();
					crosshairsFlag = true;
					mip.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(mip);
				}
			}
		} else if (e.getSource().equals(buttonExport)) {
			exportCurrentImage();
		} else if (e.getSource().equals(comboBoxSliceSelector)) {
			if (comboBoxSliceSelector.getSelectedItem().equals("All slices")) {
				comboBoxFilterSelector.removeAllItems();
				for (int i = 0; i < filterSelectorItemsAllSlices.length; i++) {
					comboBoxFilterSelector.addItem(filterSelectorItemsAllSlices[i]);
				}
			} else {
				comboBoxFilterSelector.removeAllItems();
				for (int i = 0; i < filterSelectorItemsActiveSlice.length; i++) {
					comboBoxFilterSelector.addItem(filterSelectorItemsActiveSlice[i]);
				}
			}
		}
	}

	private void applyFilter(final VtkJavaFilter filter) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		statusBar.setMessage("Applying filter " + filter.getFilterName() + "...");
		SwingWorker<VtkJavaFilter, Void> worker = new SwingWorker<VtkJavaFilter, Void>() {

			private VtkJavaFilter algorithm;

			@Override
			public VtkJavaFilter doInBackground() {
				algorithm = filter;
				algorithm.applyFilter(currentImageData);
				return algorithm;
			}

			@Override
			public void done() {
				setCursor(Cursor.getDefaultCursor());
				statusBar.setMessage("Ready");
				try {
					currentImageData = get().GetOutput();
					panel0.setInputData(currentImageData);
					panel1.setInputData(currentImageData);
					panel2.setInputData(currentImageData);
					panel0.render();
					panel1.render();
					panel2.render();
				} catch (InterruptedException ignore) {
				} catch (java.util.concurrent.ExecutionException e) {
					String why = null;
					Throwable cause = e.getCause();
					if (cause != null) {
						why = cause.getMessage();
					} else {
						why = e.getMessage();
					}
					System.err.println("Error applying filter: " + why);
				}
			}
		};
		worker.execute();
	}

	private void exportCurrentImage() {
		final String msgTemplate = "Completed %d of %d slices.\n";
		final int numberOfImagesToExport = currentImageData.GetDimensions()[2];

		class ExportTask extends SwingWorker<Void, Void>implements ChangeListener {
			@Override
			public Void doInBackground() {
				DicomExporter exporter = new DicomExporter();
				exporter.setChangeListener(this);
				exporter.exportImageData(currentImageData, "data/output/test");
				return null;
			}

			@Override
			public void done() {
				buttonExport.setEnabled(true);
			}

			@Override
			public void stateChanged(ChangeEvent e) {
				int progress = (Integer) e.getSource();
				progressMonitor.setProgress(progress);
				String message = String.format(msgTemplate, progress, numberOfImagesToExport);
				progressMonitor.setNote(message);
				if (progressMonitor.isCanceled() || isDone()) {
					if (progressMonitor.isCanceled()) {
						cancel(true);
						System.out.println("Task canceled.\n");
					} else {
						System.out.println("Task completed.\n");
					}
					buttonExport.setEnabled(true);
				}
			}
		}

		progressMonitor = new ProgressMonitor(this, "Exporting current image to DICOM",
				String.format(msgTemplate, 0, numberOfImagesToExport), 0, numberOfImagesToExport);
		progressMonitor.setProgress(0);
		buttonExport.setEnabled(false);
		ExportTask task = new ExportTask();
		task.execute();
	}

	/**
	 * Sets the message displayed in the panel's status bar at the bottom. Can
	 * be invoked safely from any thread.
	 * 
	 * @param status
	 *            The message to display
	 */
	public static void setStatusBar(final String status) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBar.setMessage(status);
			}
		});
	}

	// Load VTK library and print which library was not properly loaded
	static {
		if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
			for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
				if (!lib.IsLoaded()) {
					System.out.println(lib.GetLibraryName() + " not loaded");
				}
			}
		}
		vtkNativeLibrary.DisableOutputWindow(null);
	}
}
