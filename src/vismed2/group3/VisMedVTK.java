package vismed2.group3;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import vtk.vtkDICOMImageReader;
import vtk.vtkNativeLibrary;

public class VisMedVTK extends JPanel implements ChangeListener {
  private static final long serialVersionUID = 1L;
  private ImageViewerPanel panel0;
  private ImageViewerPanel panel1;
  private ImageViewerPanel panel2;
  private JSlider sliceSlider0;
  private JSlider sliceSlider1;
  private JSlider sliceSlider2;
  private int currentSlice0 = 0;
  private int currentSlice1 = 0;
  private int currentSlice2 = 0;

  // -----------------------------------------------------------------
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

  // -----------------------------------------------------------------
  public VisMedVTK() {
    super(new MigLayout("fill, debug"));
    
    // Get DICOM image data
    vtkDICOMImageReader dicomReader = new vtkDICOMImageReader();
    File directory = new File("data/Dentascan-0.75-H60s-3");
    dicomReader.SetDirectoryName(directory.getAbsolutePath()); //Spaces in path causing troubles
    dicomReader.Update();
    
    panel0 = new ImageViewerPanel(dicomReader.GetOutput());
    panel1 = new ImageViewerPanel(dicomReader.GetOutput());
    panel2 = new ImageViewerPanel(dicomReader.GetOutput());
    
    // Prepare slider
    JPanel sliderPanel = new JPanel(new MigLayout());
    sliceSlider0 = new JSlider(JSlider.HORIZONTAL, panel0.GetSliceMin(), panel0.GetSliceMax(), currentSlice0);
    sliceSlider0.addChangeListener(this);
    sliceSlider1 = new JSlider(JSlider.HORIZONTAL, panel1.GetSliceMin(), panel1.GetSliceMax(), currentSlice1);
    sliceSlider1.addChangeListener(this);
    sliceSlider2 = new JSlider(JSlider.HORIZONTAL, panel2.GetSliceMin(), panel2.GetSliceMax(), currentSlice2);
    sliceSlider2.addChangeListener(this);
    sliderPanel.add(sliceSlider0, "wrap");
    sliderPanel.add(sliceSlider1, "wrap");
    sliderPanel.add(sliceSlider2);

    add(panel0, "grow");
    add(panel1, "grow, wrap");
    add(panel2, "grow");
    add(sliderPanel);
    
	Runnable r1 = new Runnable() {
		public void run() {
			try {
				Thread.sleep(1000);
				panel0.GetImageViewer().GetVtkImageViewer().SetSliceOrientationToXY();
				panel1.GetImageViewer().GetVtkImageViewer().SetSliceOrientationToXZ();
				panel2.GetImageViewer().GetVtkImageViewer().SetSliceOrientationToYZ();
				panel0.GetImageViewer().GetVtkImageViewer().Render();
				panel1.GetImageViewer().GetVtkImageViewer().Render();
				panel2.GetImageViewer().GetVtkImageViewer().Render();
			} catch (InterruptedException iex) {
			}
		}
	};
	Thread thr1 = new Thread(r1);
	thr1.start();
  }

  public static void main(String s[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("VisMed2 VTK Gruppe 3");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new VisMedVTK(), BorderLayout.CENTER);
        frame.setSize(800, 800);
        frame.setVisible(true);
      }
    });
  }

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(sliceSlider0)) {
			currentSlice0 = sliceSlider0.getValue();
			panel0.setSlice(currentSlice0);
		} else if (e.getSource().equals(sliceSlider1)) {
			currentSlice1 = sliceSlider1.getValue();
			panel1.setSlice(currentSlice1);
		} else if (e.getSource().equals(sliceSlider2)) {
			currentSlice2 = sliceSlider2.getValue();
			panel2.setSlice(currentSlice2);
		}
	}
}
