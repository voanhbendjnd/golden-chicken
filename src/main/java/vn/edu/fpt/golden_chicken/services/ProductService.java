package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.ProductImage;
import vn.edu.fpt.golden_chicken.domain.request.ProductDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.CategoryRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.utils.converts.ProductConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductService {
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    FileService fileService;

    public ResultPaginationDTO fetchAllWithPagination(Pageable pageable, Specification<Product> spec) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = this.productRepository.findAll(spec, pageable);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).collect(Collectors.toList()));
        return res;
    }

    public void create(ProductDTO dto, List<MultipartFile> files, MultipartFile file)
            throws IOException, URISyntaxException {
        var category = this.categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", dto.getCategoryId()));
        var product = ProductConvert.toProduct(dto);
        product.setCategory(category);
        var allowedExtensions = Set.of(".jpg", ".png", ".jpeg");

        if (file != null && !file.isEmpty()) {
            var fileName = file.getOriginalFilename();
            var lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex == -1) {
                throw new IOException("File Invalid");
            }
            var ext = fileName.substring(lastDotIndex).toLowerCase();
            if (!allowedExtensions.contains(ext)) {
                throw new IOException("File Invalid");

            }
            product.setImage_url(this.fileService.getLastNameFile(file));

        }

        if (files != null && !files.isEmpty()) {
            var imgs = new ArrayList<ProductImage>();
            for (var x : files) {
                var fileName = x.getOriginalFilename();
                var lastDotIndex = fileName.lastIndexOf(".");
                if (lastDotIndex == -1) {
                    throw new IOException("File Invalid");
                }
                var ext = fileName.substring(lastDotIndex).toLowerCase();
                if (!allowedExtensions.contains(ext)) {
                    throw new IOException("File Invalid");
                }
                var productImg = new ProductImage();
                productImg.setProduct(product);
                productImg.setImage_url(this.fileService.getLastNameFile(x));
                imgs.add(productImg);
                product.setProductImages(imgs);
            }
        }
        this.productRepository.save(product);

    }

    @Transactional(readOnly = true)
    public List<ResProduct> getAllActiveForMenu() {
        return this.productRepository.findByActiveTrue().stream()
                .map(ProductConvert::toResProduct)
                .collect(Collectors.toList());
    }

    public ResProduct findById(long id) {
        var product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", id));
        return ProductConvert.toResProduct(product);
    }

    public void delete(long id) {
        var product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", id));
        this.productRepository.delete(product);
    }
}
