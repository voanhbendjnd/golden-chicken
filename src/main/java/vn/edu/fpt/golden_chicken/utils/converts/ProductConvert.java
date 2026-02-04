package vn.edu.fpt.golden_chicken.utils.converts;

import java.util.stream.Collectors;

import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.ProductImage;
import vn.edu.fpt.golden_chicken.domain.request.ProductDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;

public class ProductConvert {
    public static Product toProduct(ProductDTO dto) {
        var product = new Product();
        product.setActive(dto.isActive());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setType(dto.getType());
        product.setName(dto.getName());
        return product;
    }

    public static ResProduct toResProduct(Product product) {
        var res = new ResProduct();
        res.setActive(product.getActive());
        res.setDescription(product.getDescription());
        res.setId(product.getId());
        res.setName(product.getName());
        res.setPrice(product.getPrice());
        res.setType(product.getType());
        var cate = new ResProduct.Category();
        cate.setId(product.getCategory().getId());
        cate.setName(product.getCategory().getName());
        res.setCategory(cate);
        res.setImg(product.getImageUrl());
        res.setImgs(product.getProductImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));
        return res;
    }
}
