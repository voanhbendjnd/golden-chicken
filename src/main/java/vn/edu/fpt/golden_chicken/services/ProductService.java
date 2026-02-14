package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Join;
import vn.edu.fpt.golden_chicken.controllers.client.AddressController;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Category;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.ProductImage;
import vn.edu.fpt.golden_chicken.domain.request.ProductDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.CategoryRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;
import vn.edu.fpt.golden_chicken.utils.converts.ProductConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductService {

    AddressController addressController;
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    FileService fileService;

    public ResultPaginationDTO fetchAllWithPagination(Pageable pageable, Specification<Product> spec) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            return c.equal(categoryJoin.get("status"), true);
        };

        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = this.productRepository.findAll(Specification.where(spec).and(ps), pageable);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).collect(Collectors.toList()));
        return res;
    }

    public void create(ProductDTO dto, List<MultipartFile> files, MultipartFile file)
            throws IOException, URISyntaxException {
        var category = this.categoryRepository.findById(dto.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", dto.getCategory().getId()));
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
            product.setImageUrl(this.fileService.getLastNameFile(file));

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
                productImg.setImageUrl(this.fileService.getLastNameFile(x));
                imgs.add(productImg);
            }
            product.setProductImages(imgs);

        }
        product.setIsDelete(false);
        this.productRepository.save(product);

    }

    public boolean validFile(MultipartFile file) {
        var allowedExtensions = Set.of(".jpg", ".png", ".jpeg");
        var fileName = file.getOriginalFilename();
        var lastDoIndex = fileName.lastIndexOf(".");
        if (lastDoIndex == -1) {
            return false;
        }
        var ext = fileName.substring(lastDoIndex).toLowerCase();
        if (!allowedExtensions.contains(ext)) {
            return false;
        }
        return true;
    }

    public void handleGalleryUpdate(Product product, List<MultipartFile> newFiles, List<String> remainNames,
            List<String> filesToDelete) throws URISyntaxException, IOException {
        var currentImages = product.getProductImages();
        var interator = currentImages.iterator();
        while (interator.hasNext()) {
            var img = interator.next();
            if (remainNames == null || !remainNames.contains(img.getImageUrl())) {
                filesToDelete.add(img.getImageUrl());
                img.setProduct(null);
                interator.remove();
            }
        }
        if (newFiles != null && !newFiles.isEmpty()) {
            for (var x : newFiles) {
                if (x != null && !x.isEmpty() && x.getOriginalFilename() != null
                        && !x.getOriginalFilename().isBlank()) {
                    this.validFile(x);
                    var lastName = this.fileService.getLastNameFile(x);
                    var newImg = new ProductImage();
                    newImg.setImageUrl(lastName);
                    newImg.setProduct(product);
                    currentImages.add(newImg);
                }

            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ProductDTO dto, MultipartFile file, List<MultipartFile> files)
            throws IOException, URISyntaxException {
        List<String> filesToDelete = new ArrayList<>();

        // check category
        var category = this.categoryRepository.findById(dto.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", dto.getCategory().getId()));
        // get record db
        var product = this.productRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", dto.getId()));
        // check img file
        if (file != null && !file.isEmpty()) {
            // check file
            if (!this.validFile(file)) {
                throw new IOException("File Invalid");
            }

            if (product.getImageUrl() != null) {
                filesToDelete.add(product.getImageUrl());
            }
            // save new file
            product.setImageUrl(this.fileService.getLastNameFile(file));
        }
        this.handleGalleryUpdate(product, files, dto.getImgs(), filesToDelete);
        product.setCategory(category);
        product.setDescription(dto.getDescription());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setType(dto.getType());
        product.setActive(dto.isActive());
        this.productRepository.save(product);
        filesToDelete.forEach(x -> {
            try {
                this.fileService.deleteFile(x);
            } catch (IOException e) {
                System.out.println("Cannot Delete File!");
            }
        });

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

    @Transactional(readOnly = true)
    public List<ResProduct> getAllActiveForMenu() {
        return this.productRepository.findByActiveTrue().stream()
                .map(ProductConvert::toResProduct)
                .collect(Collectors.toList());
    }

    public ResultPaginationDTO fetchAllChickenHappy(Specification<Product> spec, Pageable pageable) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            var p1 = c.like(categoryJoin.get("name"), "%gà giòn%");
            var p2 = c.equal(r.get("active"), true);
            var p3 = c.equal(categoryJoin.get("status"), true);
            return c.and(p1, p2, p3);
        };
        var page = this.productRepository.findAll(Specification.where(spec).and(ps), pageable);
        var res = new ResultPaginationDTO();
        var mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        res.setMeta(mt);
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).toList());
        return res;

    }

    public ResultPaginationDTO fetchAllChickenSauce(Specification<Product> spec, Pageable pageable) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            var p1 = c.like(categoryJoin.get("name"), "%gà sốt%");
            var p2 = c.equal(r.get("active"), true);
            var p3 = c.equal(categoryJoin.get("status"), true);
            return c.and(p1, p2, p3);
        };
        var page = this.productRepository.findAll(Specification.where(spec).and(ps), pageable);
        var res = new ResultPaginationDTO();
        var mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        res.setMeta(mt);
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).toList());
        return res;

    }

    public ResultPaginationDTO fetchAllNoodle(Specification<Product> spec, Pageable pageable) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            var p1 = c.like(categoryJoin.get("name"), "%mỳ%");
            var p2 = c.equal(r.get("active"), true);
            var p3 = c.equal(categoryJoin.get("status"), true);
            return c.and(p1, p2, p3);
        };
        var page = this.productRepository.findAll(Specification.where(spec).and(ps), pageable);
        var res = new ResultPaginationDTO();
        var mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        res.setMeta(mt);
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).toList());
        return res;

    }

    public ResultPaginationDTO fetchAllComboWithPaginationAndAllStats(Specification<Product> spec, Pageable pageable) {
        Specification<Product> comboSpec = (r, q, c) -> {
            return c.equal(r.get("type"), ProductType.COMBO);
        };
        var page = this.productRepository.findAll(Specification.where(spec).and(comboSpec), pageable);
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).collect(Collectors.toList()));
        res.setMeta(meta);
        return res;
    }

    public ResultPaginationDTO fetchAllComboWithPagination(Specification<Product> spec, Pageable pageable) {
        Specification<Product> comboSpec = (r, q, c) -> {
            Join<Product, Category> join = r.join("category");
            var p1 = c.equal(r.get("type"), ProductType.COMBO);
            var p2 = c.equal(r.get("active"), true);
            var p3 = c.equal(join.get("status"), true);
            return c.and(p1, p2, p3);
        };
        var page = this.productRepository.findAll(Specification.where(spec).and(comboSpec), pageable);
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).collect(Collectors.toList()));
        res.setMeta(meta);
        return res;
    }

    public List<ResProduct> fetchAllProductSingle() {
        return this.productRepository.findByTypeAndActiveTrue(ProductType.SINGLE).stream()
                .map(ProductConvert::toResProduct)
                .collect(Collectors.toList());
    }

}