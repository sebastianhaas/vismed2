package vismed2.group3.filters;

import vtk.vtkImageData;

public class MIP implements VtkJavaFilter {

	private final vtkImageData out;
	private int sliceAlong_X;
	private int sliceAlong_Y;
	private int sliceAlong_Z;

	public MIP() {
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

		// scanline along X
		for (int width = 0; width < dims[1]; width++) {
			for (int height = 0; height < dims[0]; height++) {
				for (int slice = 0; slice < dims[2]; slice++) {
					// scanline along X
					pixelValue = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
					if (pixelValue > out.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0)) {
						out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
					}
					// scanline along Y
					if (pixelValue > out.GetScalarComponentAsDouble(height, sliceAlong_Y, slice, 0)) {
						out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
					}
					// scanline along Z
					if (pixelValue > out.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0)) {
						out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
					}
				}
			}
		}
	}

	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceAlong_X = sliceYZ;
		this.sliceAlong_Y = sliceXZ;
		this.sliceAlong_Z = sliceYX;
	}

	@Override
	public String getFilterName() {
		return "Maximum Intensity Projection";
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
