package midnightmarsbrowser.editors;

/**
 *
 */
class PanoramaCanvasRefresh implements Runnable {

    public static final int DELAY = 10;

    private PanoramaCanvas canvas;
    
    public PanoramaCanvasRefresh(PanoramaCanvas canvas) {
    	this.canvas = canvas;
	}
    
	public void run() {
        if (this.canvas != null && !this.canvas.isDisposed()) {
            this.canvas.drawView();
        }
	}
}
