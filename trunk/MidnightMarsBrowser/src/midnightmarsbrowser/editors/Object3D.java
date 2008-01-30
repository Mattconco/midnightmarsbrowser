/*
 * Modified on August 8, 2005
 */
package midnightmarsbrowser.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import midnightmarsbrowser.dialogs.UnknownExceptionDialog;

import org.eclipse.swt.examples.openglview.ImageDataUtil;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.GLU;

/**
 * Read an .obj file and accompanying .mtl file; create an OpenGL display list that can be rendered.
 * 
 * Based on an example by Jeremy Adams (elias4444) at http://lwjgl.org/forum/index.php/topic,917.30.html
 * Modified to read .mtl files, load textures, recompute surface normals, etc.
 */
public class Object3D {

	class Face {
		int[] v;
		int[] vt;
		int[] vn;
		Material mtl;
		
		Face(int[] v, int[] vt, int[] vn, Material mtl) {
			this.v = v;
			this.vt = vt;
			this.vn = vn;
			this.mtl = mtl;
		}
	}
	
	class Material {
		String name;
		float[] Ka = new float[3];
		float[] Kd = new float[3];
		float[] Ks = new float[3];
		float d = 1.0f;
		float Ns = 0.0f;
		int illum = 2;
		String map_Kd;
		int textureNumber = -1;
		boolean loaded = false;
		ArrayList loadImageBuffer = null;
		ArrayList loadImageWidth = null;
		ArrayList loadImageHeight = null;		
	}
	
	
	private ArrayList vertexsets = new ArrayList(); // Vertex Coordinates

	private ArrayList vertexsetsnorms = new ArrayList(); // Vertex
															// Coordinates
															// Normals

	private ArrayList vertexsetstexs = new ArrayList(); // Vertex Coordinates
														// Textures

	private ArrayList faces = new ArrayList(); // Array of Faces
	
	HashMap materials = new HashMap();

    IntBuffer textures;
	
	private int objectlist;

	private int numpolys = 0;
	
	URL url;
	
	int maxTextures = 15;
	boolean mipmap = true;

	// // Statisitcs for drawing ////
	public float toppoint = 0; // y+

	public float bottompoint = 0; // y-

	public float leftpoint = 0; // x-

	public float rightpoint = 0; // x+

	public float farpoint = 0; // z-

	public float nearpoint = 0; // z+

	public Object3D(URL url, boolean centerit, float translateX, float translateY, float translateZ, 
			float scaleFactor) throws IOException {
		this.url = url;
		loadObject(url);
		if (centerit) {
			centerit();
		}
		translateit(translateX, translateY, translateZ);
		if (scaleFactor != 1.0f) {
			scaleit(scaleFactor);
		}
		
		mapTextureIds();
		opengldrawtolist();
		numpolys = faces.size();
		vertexsets.clear();
		vertexsetsnorms.clear();
		vertexsetstexs.clear();
		faces.clear();
	}
	
	private void centerit() {
		float xshift = (rightpoint - leftpoint) / 2f;
		float yshift = (toppoint - bottompoint) / 2f;
		float zshift = (nearpoint - farpoint) / 2f;

		for (int i = 0; i < vertexsets.size(); i++) {
			float[] coords = (float[]) (vertexsets.get(i));
			coords[0] = coords[0] - leftpoint - xshift;
			coords[1] = coords[1] - bottompoint
					- yshift;
			coords[2] = coords[2] - farpoint - zshift;
		}
	}	
	
	private void translateit(float x, float y, float z) {
		for (int i = 0; i < vertexsets.size(); i++) {
			float[] coords = (float[]) (vertexsets.get(i));
			coords[0] = coords[0] + x;
			coords[1] = coords[1] + y;
			coords[2] = coords[2] + z;
		}
	}	
	
	private void scaleit(float scaleFactor) {
		for (int i = 0; i < vertexsets.size(); i++) {
			float[] coords = (float[]) (vertexsets.get(i));
			coords[0] = coords[0] * scaleFactor;
			coords[1] = coords[1] * scaleFactor;
			coords[2] = coords[2] * scaleFactor;
		}
	}	

	private void loadObject(URL url) throws IOException { 
        InputStream is = url.openStream(); 
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
		int linecounter = 0;
		try {
			String line;
			boolean firstpass = true;
			String[] coordstext;
			Material material = null;
			
			while (((line = br.readLine()) != null)) {
				linecounter++;
				line = line.trim();
				if (line.length() > 0) {
					if (line.startsWith("mtllib")) {
						String mtlfile = line.substring(6).trim();
						loadMtlFile(new URL(url, mtlfile));
					}
					else if (line.startsWith("usemtl")) {
						String mtlname = line.substring(6).trim();
						material = (Material) materials.get(mtlname);
					}
					else if (line.charAt(0) == 'v' && line.charAt(1) == ' ') {
						float[] coords = new float[4];
						coordstext = line.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							coords[i - 1] = Float.valueOf(coordstext[i])
									.floatValue();
						}
						// check for farpoints
						if (firstpass) {
							rightpoint = coords[0];
							leftpoint = coords[0];
							toppoint = coords[1];
							bottompoint = coords[1];
							nearpoint = coords[2];
							farpoint = coords[2];
							firstpass = false;
						}
						if (coords[0] > rightpoint) {
							rightpoint = coords[0];
						}
						if (coords[0] < leftpoint) {
							leftpoint = coords[0];
						}
						if (coords[1] > toppoint) {
							toppoint = coords[1];
						}
						if (coords[1] < bottompoint) {
							bottompoint = coords[1];
						}
						if (coords[2] > nearpoint) {
							nearpoint = coords[2];
						}
						if (coords[2] < farpoint) {
							farpoint = coords[2];
						}
						//
						vertexsets.add(coords);
					}
					else if (line.charAt(0) == 'v' && line.charAt(1) == 't') {
						float[] coords = new float[4];
						coordstext = line.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							coords[i - 1] = Float.valueOf(coordstext[i])
									.floatValue();
						}
						vertexsetstexs.add(coords);
					}
					else if (line.charAt(0) == 'v' && line.charAt(1) == 'n') {
						float[] coords = new float[4];
						coordstext = line.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							coords[i - 1] = Float.valueOf(coordstext[i])
									.floatValue();
						}
						vertexsetsnorms.add(coords);
					}
					else if (line.charAt(0) == 'f' && line.charAt(1) == ' ') {
						coordstext = line.split("\\s+");
						int[] v = new int[coordstext.length - 1];
						int[] vt = new int[coordstext.length - 1];
						int[] vn = new int[coordstext.length - 1];

						for (int i = 1; i < coordstext.length; i++) {
							String fixstring = coordstext[i].replaceAll("//",
									"/0/");
							String[] tempstring = fixstring.split("/");
							v[i - 1] = Integer.valueOf(tempstring[0])
									.intValue();
							if (tempstring.length > 1) {
								vt[i - 1] = Integer.valueOf(tempstring[1])
										.intValue();
							} else {
								vt[i - 1] = 0;
							}
							if (tempstring.length > 2) {
								vn[i - 1] = Integer.valueOf(tempstring[2])
										.intValue();
							} else {
								vn[i - 1] = 0;
							}
						}
						Face face = new Face(v, vt, vn, material);
						faces.add(face);
					}
				}
			}
			
		} catch (IOException e) {
			System.out.println("Failed to read file: " + br.toString());
		} catch (NumberFormatException e) {
			System.out.println("Malformed OBJ (on line " + linecounter + "): "
					+ br.toString() + "\r \r" + e.getMessage());
		}
	}
	
	private void loadMtlFile(URL url) throws IOException {
        InputStream is = url.openStream(); 
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
		int linecounter = 0;
		String[] params = null;
		try {
			String line;
			Material mtl = null;
			while (((line = br.readLine()) != null)) {
				linecounter++;
				line = line.trim();
				if ((line.length() == 0) || (line.startsWith("#")))
					continue;
				params = line.split("\\s+");
				if (params[0].equals("newmtl")) {
					mtl = new Material();
					mtl.name = params[1];
					materials.put(mtl.name, mtl);
				}
				else if (params[0].equals("map_Kd")) {
					mtl.map_Kd = params[1];				       	
				}
				else if (params[0].equals("Ka")) {
					Arrays.fill(mtl.Ka, 0.0f);
					for (int i = 1; i < params.length; i++) {
						mtl.Ka[i - 1] = Float.valueOf(params[i]).floatValue();
					}
				}
				else if (params[0].equals("Kd")) {
					Arrays.fill(mtl.Kd, 0.0f);
					for (int i = 1; i < params.length; i++) {
						mtl.Kd[i - 1] = Float.valueOf(params[i]).floatValue();
					}
				}
				else if (params[0].equals("Ks")) {
					Arrays.fill(mtl.Ks, 0.0f);
					for (int i = 1; i < params.length; i++) {
						mtl.Ks[i - 1] = Float.valueOf(params[i]).floatValue();
					}
				}
				else if (params[0].equals("d")) {
					mtl.d = Float.valueOf(params[1]).floatValue();
				}
				else if (params[0].equals("Ns")) {
					mtl.Ns = Float.valueOf(params[1]).floatValue();
				}
				else if (params[0].equals("illum")) {
					mtl.illum = Integer.valueOf(params[1]).intValue();
				}
			}
		} catch (IOException e) {
			System.out.println("Failed to read file: " + br.toString());
		} catch (NumberFormatException e) {
			System.out.println("Malformed MTL (on line " + linecounter + "): "
					+ br.toString() + "\r \r" + e.getMessage());
		}
	}

	/**
	 * Find texture ids for the materials
	 * @param contextUrl
	 * @throws IOException 
	 */
	private void mapTextureIds() throws IOException {
		// TODO dynamically determine number of textures used in object
		int textureCounter = 0;
        textures = BufferUtils.createIntBuffer(maxTextures);
        GL11.glGenTextures(textures);
//		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);
        Iterator iter = materials.values().iterator();
        while (iter.hasNext() && (textureCounter < maxTextures)) {
        	Material mtl = (Material) iter.next();
        	if (mtl.map_Kd == null)
        		continue;        	
			mtl.textureNumber = textures.get(textureCounter);
			textureCounter++;
        }
	}
	
	public float getXWidth() {
		float returnval = 0;
		returnval = rightpoint - leftpoint;
		return returnval;
	}

	public float getYHeight() {
		float returnval = 0;
		returnval = toppoint - bottompoint;
		return returnval;
	}

	public float getZDepth() {
		float returnval = 0;
		returnval = nearpoint - farpoint;
		return returnval;
	}

	public int numpolygons() {
		return numpolys;
	}

	public void opengldrawtolist() {
		float[] v1 = new float[3];
		float[] v2 = new float[3];
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);

		this.objectlist = GL11.glGenLists(1);

		GL11.glNewList(objectlist, GL11.GL_COMPILE);
		for (int i = 0; i < faces.size(); i++) {
			Face face = (Face) faces.get(i);
			Material mtl = face.mtl;			
			int[] tempfaces = face.v;
			int[] tempfacesnorms = face.vn;
			int[] tempfacestexs = face.vt;

			// Begin GL object
			int polytype;
			if (tempfaces.length == 3) {
				polytype = GL11.GL_TRIANGLES;
			} else if (tempfaces.length == 4) {
				polytype = GL11.GL_QUADS;
			} else {
				polytype = GL11.GL_POLYGON;
			}
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mtl.textureNumber);
			
			floatBuffer.rewind();
	        if (mtl.textureNumber >= 0) {
				floatBuffer.put(1.0f);
				floatBuffer.put(1.0f);
				floatBuffer.put(1.0f);
				floatBuffer.put(1.0f);
	        }
	        else {
	        	// using the diffuse values for the ambient for now
				floatBuffer.put(mtl.Kd[0]);
				floatBuffer.put(mtl.Kd[1]);
				floatBuffer.put(mtl.Kd[2]);
				floatBuffer.put(1.0f);
	        }
			floatBuffer.flip();
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, floatBuffer);
			
			floatBuffer.rewind();
			floatBuffer.put(mtl.Kd[0]);
			floatBuffer.put(mtl.Kd[1]);
			floatBuffer.put(mtl.Kd[2]);
			floatBuffer.put(1.0f);
			floatBuffer.flip();
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, floatBuffer);

			floatBuffer.rewind();
			floatBuffer.put(mtl.Ks[0]);
			floatBuffer.put(mtl.Ks[1]);
			floatBuffer.put(mtl.Ks[2]);
			floatBuffer.put(1.0f);
			floatBuffer.flip();
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, floatBuffer);

//	        GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, mtl.Ns);

	        GL11.glBegin(polytype);

			// compute surface normal vector; imported vertex normals appear messed up			
			float[] p0 = ((float[]) vertexsets.get(tempfaces[0] - 1));
			float[] p1 = ((float[]) vertexsets.get(tempfaces[1] - 1));
			float[] p2 = ((float[]) vertexsets.get(tempfaces[2] - 1));
			for (int n=0; n<3; n++) {
				v1[n] = p1[n] - p0[n];
				v2[n] = p2[n] - p0[n];				
			}			
			float vx = v1[1] * v2[2] - v1[2] * v2[1];			
			float vy = v1[2] * v2[0] - v1[0] * v2[2];			
			float vz = v1[0] * v2[1] - v1[1] * v2[0];
			double nl = Math.sqrt(vx*vx+vy*vy+vz*vz);
			vx = (float)(vx / nl);
			vy = (float)(vy / nl);
			vz = (float)(vz / nl);
			//
			
			for (int w = 0; w < tempfaces.length; w++) {
				if (tempfacestexs[w] != 0) {
					float textempx = ((float[]) vertexsetstexs
							.get(tempfacestexs[w] - 1))[0];
					float textempy = ((float[]) vertexsetstexs
							.get(tempfacestexs[w] - 1))[1];
					float textempz = ((float[]) vertexsetstexs
							.get(tempfacestexs[w] - 1))[2];
					GL11.glTexCoord3f(textempx, 1f - textempy, textempz);
				}
/*				
				if (tempfacesnorms[w] != 0) {
					float normtempx = ((float[]) vertexsetsnorms
							.get(tempfacesnorms[w] - 1))[0];
					float normtempy = ((float[]) vertexsetsnorms
							.get(tempfacesnorms[w] - 1))[1];
					float normtempz = ((float[]) vertexsetsnorms
							.get(tempfacesnorms[w] - 1))[2];
					
					
					GL11.glNormal3f(normtempx, normtempy, normtempz);
				}
				*/
				GL11.glNormal3f(vx, vy, vz);

				float tempx = ((float[]) vertexsets.get(tempfaces[w] - 1))[0];
				float tempy = ((float[]) vertexsets.get(tempfaces[w] - 1))[1];
				float tempz = ((float[]) vertexsets.get(tempfaces[w] - 1))[2];				
				
				GL11.glVertex3f(tempx, tempy, tempz);
			}
			
			GL11.glEnd();
		}
		GL11.glEndList();
	}

	public void opengldraw() {
		GL11.glCallList(objectlist);
	}
	
	/**
	 * Bind textures that have been pre-loaded by the Object3DImageLoader thread.
	 */
	public boolean bindTextures() {
		boolean finished = true;
		try {
	        Iterator iter = materials.values().iterator();
	        while (iter.hasNext()) {
	        	Material mtl = (Material) iter.next();
	        	if (mtl.textureNumber < 0)
	        		continue;
	        	if (!mtl.loaded) {
	        		finished = false;
	        	}
				if (mtl.loadImageBuffer != null) {
			        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mtl.textureNumber);
			        int numLevels = mtl.loadImageBuffer.size();
			        if (numLevels == 1) {
			        	int width  = ((Integer) mtl.loadImageWidth.get(0)).intValue();
			        	int height  = ((Integer) mtl.loadImageHeight.get(0)).intValue();
			        	ByteBuffer buffer = (ByteBuffer) mtl.loadImageBuffer.get(0);
			            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			        }
			        else {
				        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR/*GL11.GL_NEAREST*/);
			        	for (int level=0; level<numLevels; level++) {
				        	int width  = ((Integer) mtl.loadImageWidth.get(level)).intValue();
				        	int height  = ((Integer) mtl.loadImageHeight.get(level)).intValue();
				        	ByteBuffer buffer = (ByteBuffer) mtl.loadImageBuffer.get(level);
				            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGB8, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
			        	}
			        }
			        mtl.loadImageBuffer = null;
			        mtl.loadImageHeight = null;
			        mtl.loadImageWidth = null;
			        mtl.loaded = true;
			        break;
				}
	        }
		}
		catch (Throwable e) {
			//UnknownExceptionDialog.openDialog(this.getShell(), "Error loading image", e);
			e.printStackTrace(); 
		}
		return finished;
	}	
	

}
