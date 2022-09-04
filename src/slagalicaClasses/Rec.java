package slagalicaClasses;

public class Rec {
	private String rec;
	private boolean ubacen = false;
	
	public void ubacivanje() {
		ubacen = true;
	}
	public void setRec(String rec) {
		this.rec = rec;
	}
	public String getRec() {
		return rec;
	}
	private static char izbaciSlovo() {
		Double random1 = 65 + Math.random() * 21;
		if (random1 > 80.5) {
			random1++;
		}
		if (random1 > 86) {
			random1 = 90.0;
		}
		return (char) Math.round(random1);
	}
	public Rec() {
		rec = "";
		for (int i = 0; i < 12; i++) {
				rec = rec + izbaciSlovo();
		}
	}
}
