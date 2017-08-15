package by.bsuir.evgeny.managerfridge.entity;

public class Name {
    private String name_product;
    private boolean check_product;

    public String getName_product() {
        return name_product;
    }

    public void setName_product(String name_product) {
        this.name_product = name_product;
    }

    public boolean isCheck_product() {
        return check_product;
    }

    public void setCheck_product(boolean check_product) {
        this.check_product = check_product;
    }

    public Name(String name, boolean check) {
        this.name_product=name;
        this.check_product=check;
    }
}