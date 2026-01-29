package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.ProductImage;
import vn.edu.fpt.golden_chicken.domain.request.ProductDTO;
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
        if (!file.isEmpty() && file != null) {
            product.setImage_url(this.fileService.getLastNameFile(file));

        }
        if (!files.isEmpty() || files != null) {
            var imgs = new ArrayList<ProductImage>();
            for (var x : files) {
                var productImg = new ProductImage();
                productImg.setProduct(product);
                productImg.setImage_url(this.fileService.getLastNameFile(x));
                imgs.add(productImg);
            }
        }
        this.productRepository.save(product);

    }
}
