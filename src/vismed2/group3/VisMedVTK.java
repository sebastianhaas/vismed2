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
  private int currentSlice0 = 0;

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
    
    panel0 = new ImageViewerPanel(ImageViewerPanel.ORIENTATION_XY, dicomReader.GetOutput());
    panel1 = new ImageViewerPanel(ImageViewerPanel.ORIENTATION_XZ, dicomReader.GetOutput());
    panel2 = new ImageViewerPanel(ImageViewerPanel.ORIENTATION_YZ, dicomReader.GetOutput());
    
    // Prepare slider
    sliceSlider0 = new JSlider(JSlider.HORIZONTAL, panel0.GetSliceMin(), panel0.GetSliceMax(), currentSlice0);
    sliceSlider0.addChangeListener(this);

    add(panel0, "grow");
    add(panel1, "grow, wrap");
    add(panel2, "grow");
    add(sliceSlider0);
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
		}
	}
}
