package vismed2.group3.filters;

import java.util.Arrays;

import vtk.vtkImageData;

/**
 * The ThresholdFilter sets all pixels below the threshold as background, all
 * pixels above stay are given the original value
 */
public class ThresholdFilter implements VtkJavaFilter {

	private final vtkImageData out;
	private double upperThreshold = 0;
	private double lowerThreshold = 0;
	private int sliceAlong_X = 0;
	private int sliceAlong_Y = 0;
	private int sliceAlong_Z = 0;
	private boolean doAllSlices = false;

	public ThresholdFilter() {
		out = new vtkImageData();
	}

	@Override
	public void applyFilter(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);
		double pixelValue;
		
		// iterate over image and set every pixel 0 which isn't bigger than
		// the threshold
		// for perforamce reasons only relevant slices are considered
		if (doAllSlices) {
			for (int slice = 0; slice < dims[2]; slice++) {
				for (int width = 0; width < dims[1]; width++) {
					for (int height = 0; height < dims[0]; height++) {
						pixelValue = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
						if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
							out.SetScalarComponentFromDouble(height, width, slice, 0, pixelValue);
						} else {
							out.SetScalarComponentFromDouble(height, width, slice, 0, 0);
						}
					}
				}
			}
		} else {

			// iterate over relevant slices and set every pixel 0 which isn't
			// bigger
			// than the threshold (YZ)
			for (int width = 0; width < dims[1]; width++) {
				for (int height = 0; height < dims[0]; height++) {
					pixelValue = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
					if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
						out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
					} else {
						out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, 0);
					}
				}
			}

			// do filter for the pannel1 YX
			for (int width = 0; width < dims[2]; width++) {
				for (int height = 0; height < dims[0]; height++) {
					pixelValue = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, width, 0);
					if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
						out.SetScalarComponentFromDouble(height, sliceAlong_Y, width, 0, pixelValue);
					} else {
						out.SetScalarComponentFromDouble(height, sliceAlong_Y, width, 0, 0);
					}
				}
			}

			// do filter for the pannel1 YX
			for (int width = 0; width < dims[1]; width++) {
				for (int height = 0; height < dims[0]; height++) {
					pixelValue = imgData.GetScalarComponentAsDouble(sliceAlong_Z, height, width, 0);
					if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
						out.SetScalarComponentFromDouble(sliceAlong_Z, height, width, 0, pixelValue);
					} else {
						out.SetScalarComponentFromDouble(sliceAlong_Z, height, width, 0, 0);
					}
				}
			}
		}
	}

	public void setAllSlices(boolean doAllSlices) {
		this.doAllSlices = doAllSlices;
	}

	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceAlong_X = sliceYZ;
		this.sliceAlong_Y = sliceXZ;
		this.sliceAlong_Z = sliceYX;
	}

	public void setUpperThreshold(double threshold) {
		this.upperThreshold = threshold;
	}

	public void setLowerThreshold(double threshold) {
		this.lowerThreshold = threshold;
	}

	@Override
	public String getFilterName() {
		return "Threshold";
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
