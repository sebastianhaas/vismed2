package vismed2.group3.filters;

import vtk.vtkImageData;

/**
 * 
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 *
 */
public interface VtkJavaFilter {

	/**
	 * Apply the filter on the given vtkImageData.
	 * The Filtered image can be accessed via GetOutput()
	 * 
	 * @param imgData
	 */
	public void applyFilter(vtkImageData imgData);
	
	/**
	 * Return the name of the filter as String
	 */
	public String getFilterName();
	
	/**
	 * return the result of the filter
	 */
	public vtkImageData GetOutput();
}
