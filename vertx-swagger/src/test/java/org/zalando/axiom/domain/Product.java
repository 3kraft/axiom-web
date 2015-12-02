package org.zalando.axiom.domain;

public class Product {

    private String productId;

    private String description;

    private String displayName;

    private String capacity;

    private String image;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (productId != null ? !productId.equals(product.productId) : product.productId != null) return false;
        if (description != null ? !description.equals(product.description) : product.description != null) return false;
        if (displayName != null ? !displayName.equals(product.displayName) : product.displayName != null) return false;
        if (capacity != null ? !capacity.equals(product.capacity) : product.capacity != null) return false;
        return !(image != null ? !image.equals(product.image) : product.image != null);

    }

    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }
}
