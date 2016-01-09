package vismed2.group3.filters;

import vtk.vtkImageData;

public class GradientFilter implements VtkJavaFilter {

	private final vtkImageData out;

	private String filterName = "Roberts";
	private int sliceAlong_X = 0;
	private int sliceAlong_Y = 0;
	private int sliceAlong_Z = 0;
	private boolean doAllSlices = false;

//	public void SetKernelSize(int height, int width, int depth) {
//		this.filter_height = height;
//		this.filter_width = width;
//		this.filter_depth = depth;
//	}

	public void setDoAllSlices(boolean doAllSlices) {
		this.doAllSlices = doAllSlices;
	}

	public GradientFilter() {
		out = new vtkImageData();
	}

	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceAlong_X = sliceYZ;
		this.sliceAlong_Y = sliceXZ;
		this.sliceAlong_Z = sliceYX;
	}

	@Override
	public void applyFilter(vtkImageData imgData) {
		if (this.filterName.equals("Roberts")) {
			doRoberts(imgData);
		}
		// TODO other Gradient Filters
	}

	/**
	 * Roberts' cross Operator: 
	 *  0 1 | 1 0 
	 * -1 0 | 0 -1
	 */
	private void doRoberts(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);
		
		double pixelValue1;
		double pixelValue2;
		double pixelValue3;
		double pixelValue4;
		double pixelValue;

		if (doAllSlices) { // iterate through the image/ through all slices
			for (int slice = 0; slice < dims[2]; slice++) {
				for (int width = 0; width < dims[1] - 1; width++) {
					for (int height = 0; height < dims[0] - 1; height++) {
						System.err.println("to be done...");
//						pixelValue1 = imgData.GetScalarComponentAsDouble(height, width+1, slice, 0);
//						pixelValue2 = imgData.GetScalarComponentAsDouble(height+1, width, slice, 0) * (-1);
//						
//						pixelValue = pixelValue1 / 4 + pixelValue2 / 4;
//						out.SetScalarComponentFromDouble(height, width, slice, 0, pixelValue);
					}
				}
			}
		} else { // only do active slice
			for (int slice = 0; slice < dims[2]; slice++) {
				for (int width = 0; width < dims[1]; width++) {
					for (int height = 0; height < dims[0]; height++) {
						if (slice == sliceAlong_X) { // along X
							pixelValue1 = imgData.GetScalarComponentAsDouble(height, width+1, sliceAlong_X, 0);
							pixelValue2 = imgData.GetScalarComponentAsDouble(height+1, width, sliceAlong_X, 0) * (-1);
							
							pixelValue3 = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
							pixelValue4 = imgData.GetScalarComponentAsDouble(height+1, width+1, sliceAlong_X, 0) * (-1);
							
							pixelValue = (pixelValue1 + pixelValue2 + pixelValue3 + pixelValue4)/8;
							out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
						}
						if (width == sliceAlong_Y) {
							pixelValue1 = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice, 0);
							pixelValue2 = imgData.GetScalarComponentAsDouble(height+1, sliceAlong_Y, slice+1, 0) * (-1);
							
							pixelValue3 = imgData.GetScalarComponentAsDouble(height+1, sliceAlong_Y, slice, 0);
							pixelValue4 = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice+1, 0) * (-1);
							
							pixelValue = (pixelValue1 + pixelValue2 + pixelValue3 + pixelValue4) / 8;
							out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
						}
						if (height == sliceAlong_Z) {
							pixelValue = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0);
							pixelValue1 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width+1, slice, 0);
							pixelValue2 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice+1, 0) * (-1);
							
							pixelValue3 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0);
							pixelValue4 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width+1, slice+1, 0) * (-1);
							
							pixelValue = (pixelValue1 + pixelValue2 + pixelValue3 + pixelValue4) / 8;
							out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
						}
					}
				}
			}
		}
	}

	/**
	 * filterName hast to be one of the following: 
	 * 1) Roberts
	 * @param filter
	 */
	public void setFilter(String filterName) {
		this.filterName = filterName;
	}
	
	public boolean setAllSlices(boolean doAllSlices) {
		this.doAllSlices = doAllSlices;
		if (doAllSlices) return false;
		else return true;
	}
	
	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
