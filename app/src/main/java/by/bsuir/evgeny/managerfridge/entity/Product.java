package by.bsuir.evgeny.managerfridge.entity;

public class Product {
    private String product_name;
    private String product_descripton;
    private boolean check_product;
    public Product(String _name, String _description, boolean _check) {
        product_name=_name;
        product_descripton=_description;
        check_product=_check;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_descripton() {
        return product_descripton;
    }

    public void setProduct_descripton(String product_descripton) {
        this.product_descripton = product_descripton;
    }

    public boolean isCheck_product() {
        return check_product;
    }

    public void setCheck_product(boolean check_product) {
        this.check_product = check_product;
    }
}