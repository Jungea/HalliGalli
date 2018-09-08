package halligalli;

import javax.swing.JFrame;

public class HGClient extends JFrame {

	public HGClient() {
		setTitle("Halli Galli");
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		HGClient client = new HGClient();
		client.setSize(800,600);
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setVisible(true);
	}

}
