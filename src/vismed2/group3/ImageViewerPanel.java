package vismed2.group3;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import vtk.vtkCanvas;
import vtk.vtkImageData;
import vtk.vtkImageViewer2;

/**
 * A wrapper class to use a VTK image view in Swing applications. Extends
 * {@link javax.swing.JComponent} and draws a {@link vtk.vtkImageViewer2} using
 * {@link vtk.vtkCanvas}. The image to be displayed can be drawn with different
 * orientation, brightness, contrast and zoom level using VTK's built-in
 * functionality.
 * 
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 */
public class ImageViewerPanel extends JComponent {

	/**
	 * Represents XY-orientation.
	 */
	public static final int ORIENTATION_XY = 2;

	/**
	 * Represents XZ-orientation.
	 */
	public static final int ORIENTATION_XZ = 1;

	/**
	 * Represents YZ-orientation.
	 */
	public static final int ORIENTATION_YZ = 0;
	private static final long serialVersionUID = -6951851465094754751L;
	private VtkImageViewer2Java imageViewer;

	/**
	 * Initializes a new image viewer panel showing the given image data. It
	 * might be necessary to wait for the native view to finish initialization
	 * before being able to access any methods or properties.
	 * 
	 * @param imageData
	 *            The image to be shown
	 */
	public ImageViewerPanel(vtkImageData imageData) {
		setLayout(new BorderLayout());
		imageViewer = new VtkImageViewer2Java(imageData);
		add(imageViewer, BorderLayout.CENTER);
	}

	/**
	 * Returns the underlying image viewer instance.
	 * 
	 * @return The underlying image viewer instance.
	 */
	// public VtkImageViewer2Java getImageViewer() {
	// return imageViewer;
	// }

	/**
	 * Sets the slice to be displayed.
	 * 
	 * @param slice
	 *            The slice to be displayed
	 */
	public void setSlice(int slice) {
		imageViewer.GetVtkImageViewer().SetSlice(slice);
	}

	/**
	 * Returns the view's current orientation.
	 * 
	 * @return An integer constant indicating the view's current orientation.
	 *         This classes public constants can be used for that purpose.
	 * @see ImageViewerPanel#ORIENTATION_XY
	 * @see ImageViewerPanel#ORIENTATION_XZ
	 * @see ImageViewerPanel#ORIENTATION_YZ
	 */
	public int getSliceOrientation() {
		return imageViewer.GetVtkImageViewer().GetSliceOrientation();
	}

	/**
	 * Sets the view's orientation.
	 * 
	 * @param orientation
	 *            An integer constant indicating the view's current orientation.
	 *            This classes public constants can be used for that purpose.
	 * @see ImageViewerPanel#ORIENTATION_XY
	 * @see ImageViewerPanel#ORIENTATION_XZ
	 * @see ImageViewerPanel#ORIENTATION_YZ
	 */
	public void setSliceOrientation(int orientation) {
		imageViewer.GetVtkImageViewer().SetSliceOrientation(orientation);
	}

	/**
	 * Returns the maximum slice number of the image given the current
	 * orientation.
	 * 
	 * @return The maximum slice number
	 */
	public int getSliceMax() {
		return imageViewer.GetVtkImageViewer().GetSliceMax();
	}

	/**
	 * Returns the minimum slice number of the image given the current
	 * orientation.
	 * 
	 * @return The minimum slice number
	 */
	public int getSliceMin() {
		return imageViewer.GetVtkImageViewer().GetSliceMin();
	}

	/**
	 * Returns the currently displayed slice.
	 * 
	 * @return The number of the currently displayed slice
	 */
	public int getSlice() {
		return imageViewer.GetVtkImageViewer().GetSlice();
	}

	/**
	 * Sets the image to be displayed.
	 * 
	 * @param data
	 *            The image data to be displayed
	 */
	public void setInputData(vtkImageData data) {
		imageViewer.GetVtkImageViewer().SetInputData(data);
		imageViewer.GetVtkImageViewer().UpdateDisplayExtent();
	}

	/**
	 * Renders (refreshes) the underlying VTK view.
	 */
	public void render() {
		imageViewer.GetVtkImageViewer().Render();
	}
}

/**
 * An additional wrapper class to display VTK's image viewer. Use
 * ImageViewerPanel directly.
 *
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 * 
 */
class VtkImageViewer2Java extends vtkCanvas {

	private static final long serialVersionUID = 1L;
	private vtkImageViewer2 imageViewer;

	VtkImageViewer2Java(vtkImageData imageData) {
		imageViewer = new vtkImageViewer2();
		imageViewer.SetInputData(imageData);
		imageViewer.SetRenderWindow(GetRenderWindow());
		imageViewer.SetRenderer(GetRenderer());
		imageViewer.SetupInteractor(getRenderWindowInteractor());
		imageViewer.UpdateDisplayExtent();
	}

	public void repaint() {
		imageViewer.UpdateDisplayExtent();
		imageViewer.Render();
		super.repaint();
	}

	vtkImageViewer2 GetVtkImageViewer() {
		return imageViewer;
	}

}