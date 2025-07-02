package gui;

public class PRODUCT {
    protected String name;
    protected String type;

    public PRODUCT(String name, String type) {
        this.name = name.toUpperCase(); // normalize case
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Product{name='" + name + "', type='" + type + "'}";
    }
}
