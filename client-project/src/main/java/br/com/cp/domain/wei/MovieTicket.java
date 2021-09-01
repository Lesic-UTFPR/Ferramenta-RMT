package br.com.cp.domain.wei;

public class MovieTicket {
	
	private double price;
	
	public double calculate(char type) {
		
		if(type == 'S') {
			return price * 0.8;
		} else if(type == 'C') {
			return price-10;
		} else if(type == 'M') {
			return price * 0.5;
		}
		return -1;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public double getPrice() {
		return price;
	}

}
