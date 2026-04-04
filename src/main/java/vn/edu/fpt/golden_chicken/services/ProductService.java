package vn.edu.fpt.golden_chicken.services;

import jakarta.persistence.criteria.Join;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.entity.Category;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.ProductImage;
import vn.edu.fpt.golden_chicken.domain.request.ProductDTO;
import vn.edu.fpt.golden_chicken.domain.response.ProductSearchSuggestionDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.*;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;
import vn.edu.fpt.golden_chicken.utils.converts.ProductConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.BadRequestExceptionCustomer;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductService {
    ReviewService reviewService;
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    FileService fileService;
    CartItemRepository cartItemRepository;
    OrderItemRepository orderItemRepository;
    ComboDetailRepository comboDetailRepository;

    public List<ProductSearchSuggestionDTO> searchByName(String name) {
        var products = productRepository.findByNameContainingIgnoreCaseAndActiveTrueAndCategory_StatusTrue(name);
        return products.stream()
                .limit(8)
                .map(p -> new ProductSearchSuggestionDTO(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }

    public void updateStatus(Long id) {
        var product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", id));
        var status = !product.getActive();
        product.setActive(status);
        if (this.comboDetailRepository.existsByProductId(id)
                || this.comboDetailRepository.existsByComboId(id)) {

            if (!status) {
                var combos = this.comboDetailRepository.findByProductId(id);
                for (var x : combos) {
                    x.getCombo().setActive(false);
                }
                this.comboDetailRepository.saveAll(combos);
            }
        }
        this.productRepository.save(product);
    }

    public ResultPaginationDTO fetchAllWithPaginationNewProduct(Specification<Product> spec) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            var r1 = c.equal(categoryJoin.get("status"), true);
            var r2 = c.equal(r.get("active"), true);
            return c.and(r1, r2);
        };
        var pageable = PageRequest.of(0, 5, Sort.by("id").descending());
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = this.productRepository.findAll(ps, pageable);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(ProductConvert::toResProduct).collect(Collectors.toList()));
        return res;
    }

    public ResultPaginationDTO fetchAllWithPaginationBestSeller(Specification<Product> spec) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            var r1 = c.equal(categoryJoin.get("status"), true);
            var r2 = c.equal(r.get("active"), true);
            return c.and(r1, r2);
        };
        var pageable = PageRequest.of(0, 10, Sort.by("sold").descending());
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

    public ResultPaginationDTO fetchAllWithPaginationHasCombo(Pageable pageable, Specification<Product> spec) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            var r1 = c.equal(categoryJoin.get("status"), true);
            var r2 = c.equal(r.get("active"), true);
            return c.and(r1, r2);
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

    public ResultPaginationDTO fetchAllWithPagination(Pageable pageable, Specification<Product> spec) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            // var p1 = c.equal(categoryJoin.get("status"), true);
            var p2 = c.equal(r.get("type"), ProductType.SINGLE);
            return c.and(p2);
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

    public void create(ProductDTO dto, List<MultipartFile> files, MultipartFile file, boolean isCombo)
            throws IOException, URISyntaxException {
        var category = this.categoryRepository.findById(dto.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thể loại", dto.getCategory().getId()));
        if (this.productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DataInvalidException("Sản phẩm với tên (" + dto.getName() + ") đã tồn tại!");
        }
        var product = ProductConvert.toProduct(dto);
        product.setCategory(category);
        if (isCombo) {
            product.setType(ProductType.COMBO);
        } else {
            product.setType(ProductType.SINGLE);

        }
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
            product.setImageUrl(this.fileService.getLastNameFile(file, DeclareConstant.productFolder));

        }

        if (files != null && !files.isEmpty() && !files.getFirst().getOriginalFilename().isEmpty()) {
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
                productImg.setImageUrl(this.fileService.getLastNameFile(x, DeclareConstant.productFolder));
                imgs.add(productImg);
            }
            product.setProductImages(imgs);

        }
        this.productRepository.save(product);

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
                    this.fileService.validFile(x);
                    var lastName = this.fileService.getLastNameFile(x, DeclareConstant.productFolder);
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
        if (this.productRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), dto.getId())) {
            throw new DataInvalidException("Sản phẩm với tên (" + dto.getName() + ") đã tồn tại!");
        }
        List<String> filesToDelete = new ArrayList<>();

        var category = this.categoryRepository.findById(dto.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thể lại", dto.getCategory().getId()));
        var product = this.productRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", dto.getId()));
        if (file != null && !file.isEmpty()) {
            if (!this.fileService.validFile(file)) {
                throw new IOException("File Invalid");
            }

            if (product.getImageUrl() != null) {
                filesToDelete.add(product.getImageUrl());
            }
            product.setImageUrl(this.fileService.getLastNameFile(file, DeclareConstant.productFolder));
        }
        this.handleGalleryUpdate(product, files, dto.getImgs(), filesToDelete);
        product.setCategory(category);
        product.setDescription(dto.getDescription());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        // product.setType(dto.getType());
        product.setActive(dto.isActive());
        this.productRepository.save(product);
        filesToDelete.forEach(x -> {
            try {
                this.fileService.deleteFile(DeclareConstant.productFolder, x);
            } catch (IOException e) {
                System.out.println("Cannot Delete File!");
            }
        });

    }

    public ResProduct findById(long id) {
        var product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", id));
        if (product.getActive()) {
            this.reviewService.syncProductRating(id);

            return ProductConvert.toResProduct(product);

        } else {
            throw new BadRequestExceptionCustomer("Sản phẩm đã ngưng hoạt động!");
        }
    }

    public ResProduct findByIdWithCombo(long id) {
        var product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", id));
        if (product.getType() == ProductType.COMBO) {
            this.reviewService.syncProductRating(id);
            return ProductConvert.toResProduct(product);
        } else {
            throw new BadRequestExceptionCustomer("Sản phẩm đã ngưng hoạt động!");
        }
    }

    public void delete(long id) {
        var product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", id));
        if (this.orderItemRepository.existsByProductId(id) || this.cartItemRepository.existsByProductId(id)
                || this.comboDetailRepository.existsByProductId(id) || this.comboDetailRepository.existsByComboId(id)) {
            product.setActive(false);
            var combos = this.comboDetailRepository.findByProductId(id);
            for (var x : combos) {
                x.getCombo().setActive(false);
            }
            this.comboDetailRepository.saveAll(combos);
            return;
        } else {
            this.productRepository.delete(product);
        }
    }

    @Transactional(readOnly = true)
    public List<ResProduct> getAllActiveForMenu() {
        return this.productRepository.findByActiveTrueAndCategoryStatusTrue().stream()
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
            var p1 = c.like(categoryJoin.get("name"), "%sốt%");
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
            var p1 = c.like(categoryJoin.get("name"), "%noodles%");
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

    public ResultPaginationDTO fetchAllLowMeal(Specification<Product> spec, Pageable pageable) {
        Specification<Product> ps = (r, q, c) -> {
            Join<Product, Category> categoryJoin = r.join("category");
            var p1 = c.like(categoryJoin.get("name"), "%tráng%");
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

    public List<ResProduct> relationshipByCategory(Long id) {
        var product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", id));
        var categoryName = product.getCategory().getName();
        var products = this.productRepository.findRelatedProducts(categoryName, id);
        if (products.size() < 5) {
            products.addAll(this.productRepository.findByTopSold(id));
        }
        return products.stream().map(ProductConvert::toResProduct).toList();
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

    public boolean checkProductAndCategoryActive(Long id) {
        var productOptional = this.productRepository.findById(id);
        if (productOptional.isPresent()) {
            var product = productOptional.get();
            var cate = product.getCategory();
            if (cate != null) {
                if (product.getActive() && cate.getStatus()) {
                    return true;
                }
            }

        }
        return false;
    }

}