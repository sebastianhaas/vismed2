package vismed2.group3.filters;

import vtk.vtkImageData;

/**
 * The ThresholdFilter sets all pixels below the threshold as background, all
 * pixels above stay are given the original value
 */
public class ThresholdFilter implements VtkJavaFilter {

	private final vtkImageData out;
	private double upperThreshold = 0;
	private double lowerThreshold = 0;
	private int sliceYZ = 0;
	private int sliceXZ = 0;
	private int sliceYX = 0;

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

		// iterate over image and set every pixel 0 which isn't bigger than
		// the threshold
		/* for perforamce reasons only relevant slices are considered
		
		for (int slice = 0; slice < dims[2]; slice++) {
			for (int width = 0; width < dims[1]; width++) {
				for (int height = 0; height < dims[0]; height++) {
					double pixelValue = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
					if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
						out.SetScalarComponentFromDouble(height, width, slice, 0, pixelValue);
					} else {
						out.SetScalarComponentFromDouble(height, width, slice, 0, 0);
					}
				}
			}
		}
*/ 
		
		// iterate over relevant slices and set every pixel 0 which isn't bigger
		// than the threshold
		for (int width = 0; width < dims[1]; width++) {
			for (int height = 0; height < dims[0]; height++) {
				double pixelValue = imgData.GetScalarComponentAsDouble(height, width, sliceYZ, 0);
				if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
					out.SetScalarComponentFromDouble(height, width, sliceYZ, 0, pixelValue);
				} else {
					out.SetScalarComponentFromDouble(height, width, sliceYZ, 0, 0);
				}
			}
		}
		for (int depth = 0; depth < dims[2]; depth++) {
			for (int height = 0; height < dims[0]; height++) {
				double pixelValue = imgData.GetScalarComponentAsDouble(height, depth, sliceXZ, 0);
				if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
					out.SetScalarComponentFromDouble(height, depth, sliceXZ, 0, pixelValue);
				} else {
					out.SetScalarComponentFromDouble(height, sliceXZ, depth, 0, 0);
				}
			}
		}
		for (int width = 0; width < dims[1]; width++) {
			for (int height = 0; height < dims[0]; height++) {
				double pixelValue = imgData.GetScalarComponentAsDouble(height, width, sliceYX, 0);
				if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
					out.SetScalarComponentFromDouble(height, width, sliceYX, 0, pixelValue);
				} else {
					out.SetScalarComponentFromDouble(sliceYX, height, width, 0, 0);
				}
			}
		}

	}

	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceYZ = sliceYZ;
		this.sliceXZ = sliceXZ;
		this.sliceYX = sliceYX;
	}

	public void setUpperThreshold(double threshold) {
		this.upperThreshold = threshold;
	}

	public void setLowerThreshold(double threshold) {
		this.lowerThreshold = threshold;
	}

	@Override
	public String getFilterName() {
		return "Threshold Filter";
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
