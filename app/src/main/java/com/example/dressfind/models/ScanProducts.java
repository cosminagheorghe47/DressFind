package com.example.dressfind.models;

public class ScanProducts {

    private String ScanId;
    private String ProductId;

    public ScanProducts() {
    }

    public ScanProducts(String scanId, String productId) {
        ScanId = scanId;
        ProductId = productId;
    }

    public String getScanId() {
        return ScanId;
    }

    public void setScanId(String scanId) {
        ScanId = scanId;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }
}
