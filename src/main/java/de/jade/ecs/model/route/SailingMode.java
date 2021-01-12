package de.jade.ecs.model.route;

/** 
 * 
 * @author chris
 *
 */
public enum SailingMode {
	
	LOXODROME(0, "Loxodrome"),
	ORTHODROME(1,"Orthodrome");
	
	private int id;
	private String name;
	
	SailingMode(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
	    return id;
	}

	public String getName() {
	    return name;
	}

}
