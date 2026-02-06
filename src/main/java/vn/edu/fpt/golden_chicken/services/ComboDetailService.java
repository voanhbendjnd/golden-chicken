package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.ComboDetail;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.request.ComboDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResSingleProduct;
import vn.edu.fpt.golden_chicken.repositories.CategoryRepository;
import vn.edu.fpt.golden_chicken.repositories.ComboDetailRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ComboDetailService {
    ComboDetailRepository comboDetailRepository;
    ProductService productService;
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    FileService fileService;

    @Transactional(rollbackFor = Exception.class)
    public void update(ComboDTO dto, MultipartFile file, List<MultipartFile> files)
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
            if (!this.productService.validFile(file)) {
                throw new IOException("File Invalid");
            }

            if (product.getImageUrl() != null) {
                filesToDelete.add(product.getImageUrl());
            }
            // save new file
            product.setImageUrl(this.fileService.getLastNameFile(file));
        }
        this.productService.handleGalleryUpdate(product, files, dto.getImgs(), filesToDelete);
        product.setCategory(category);
        product.setDescription(dto.getDescription());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setType(dto.getType());
        product.setActive(dto.isActive());
        var lastProduct = this.productRepository.save(product);
        filesToDelete.forEach(x -> {
            try {
                this.fileService.deleteFile(x);
            } catch (IOException e) {
                System.out.println("Cannot Delete File!");
            }
        });
        var comboDetails = new ArrayList<ComboDetail>();
        if (dto.getId() != null) {
            this.comboDetailRepository.deleteByComboId(lastProduct.getId());
        }
        Map<Long, Product> productMp = this.productRepository
                .findByIdIn(dto.getItems().stream().map(x -> x.getId()).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Product::getId, p -> p));
        var details = dto.getItems().stream().map(it -> {
            var singleProduct = productMp.get(it.getId());
            if (singleProduct == null) {
                throw new ResourceNotFoundException("Product ID", it.getId());
            }
            var detail = new ComboDetail();
            detail.setCombo(lastProduct);
            detail.setProduct(singleProduct);
            detail.setQuantity(it.getQuantity());
            return detail;

        }).collect(Collectors.toList());
        this.comboDetailRepository.saveAll(details);
    }

    public List<ResSingleProduct> getProductInCombo(long comboId) {
        var details = this.comboDetailRepository.findByComboId(comboId);
        return details.stream().map(x -> {
            var product = x.getProduct();
            var res = new ResSingleProduct();
            res.setId(product.getId());
            res.setActive(product.getActive());
            res.setImg(product.getImageUrl());
            res.setName(product.getName());
            res.setPrice(product.getPrice());
            res.setQuantity(x.getQuantity());
            return res;
        }).collect(Collectors.toList());
    }
}
