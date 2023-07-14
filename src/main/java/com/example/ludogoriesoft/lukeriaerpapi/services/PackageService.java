package com.example.ludogoriesoft.lukeriaerpapi.services;


import com.example.ludogoriesoft.lukeriaerpapi.dtos.PackageDTO;
import com.example.ludogoriesoft.lukeriaerpapi.models.Package;
import com.example.ludogoriesoft.lukeriaerpapi.models.Plate;
import com.example.ludogoriesoft.lukeriaerpapi.repository.CartonRepository;
import com.example.ludogoriesoft.lukeriaerpapi.repository.PackageRepository;
import com.example.ludogoriesoft.lukeriaerpapi.repository.PlateRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PackageService {
    private final PackageRepository packageRepository;
    private final CartonRepository cartonRepository;
    private final PlateRepository plateRepository;
    private final ModelMapper modelMapper;

    public List<PackageDTO> getAllPackages() {
        List<Package> packages = packageRepository.findByDeletedFalse();
        return packages.stream().map(package1 -> modelMapper.map(package1, PackageDTO.class)).toList();
    }

    public PackageDTO getPackageById(Long id) throws ChangeSetPersister.NotFoundException {
        Package package1 = packageRepository.findByIdAndDeletedFalse(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        return modelMapper.map(package1, PackageDTO.class);
    }

    public PackageDTO createPackage(PackageDTO packageDTO) {
        if (StringUtils.isBlank(packageDTO.getName())) {
            throw new ValidationException("Name is required");
        }

        if (packageDTO.getPiecesCarton() == 0) {
            throw new ValidationException("Pieces of carton must be greater than zero");
        }
        if (packageDTO.getAvailableQuantity() == 0) {
            throw new ValidationException("Available quantity be greater than zero");
        }
        if (packageDTO.getPrice() == 0) {
            throw new ValidationException("Price must be greater than zero");
        }
        if (packageDTO.getCartonId() != null) {
            boolean cartonExists = cartonRepository.existsById(packageDTO.getCartonId());
            if (!cartonExists) {
                throw new ValidationException("Carton does not exist with ID: " + packageDTO.getCartonId());
            }
        } else {
            throw new ValidationException("Carton ID cannot be null!");
        }
        if (packageDTO.getPlateId() != null) {
            boolean plateExists = plateRepository.existsById(packageDTO.getPlateId());
            if (!plateExists) {
                throw new ValidationException("Plate does not exist with ID: " + packageDTO.getPlateId());
            }
        } else {
            throw new ValidationException("Plate ID cannot be null!");
        }
        Package packageEntity = packageRepository.save(modelMapper.map(packageDTO, Package.class));
        return modelMapper.map(packageEntity, PackageDTO.class);
    }

    public PackageDTO updatePackage(Long id, PackageDTO packageDTO) throws ChangeSetPersister.NotFoundException {
        Package existingPackage = packageRepository.findByIdAndDeletedFalse(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        if (StringUtils.isBlank(packageDTO.getName())) {
            throw new ValidationException("Name is required!");
        }
        if (packageDTO.getPiecesCarton() <= 0) {
            throw new ValidationException("This field must be greater than zero!");
        }
        if (packageDTO.getPrice() == 0) {
            throw new ValidationException("Price must be greater than zero!");
        }
        if (packageDTO.getAvailableQuantity() <= 0) {
            throw new ValidationException("Available quantity be greater than zero!");
        }
        if (packageDTO.getCartonId() != null) {
            boolean cartonExists = cartonRepository.existsById(packageDTO.getCartonId());
            if (!cartonExists) {
                throw new ValidationException("Carton does not exist with ID: " + packageDTO.getCartonId());
            }
        } else {
            throw new ValidationException("Carton ID cannot be null!");
        }
        if (packageDTO.getPlateId() != null) {
            boolean plateExists = plateRepository.existsById(packageDTO.getCartonId());
            if (!plateExists) {
                throw new ValidationException("Plate does not exist with ID: " + packageDTO.getCartonId());
            }
        } else {
            throw new ValidationException("Plate ID cannot be null!");
        }
        existingPackage.setName(packageDTO.getName());
        existingPackage.setAvailableQuantity(packageDTO.getAvailableQuantity());
        existingPackage.setPrice(packageDTO.getPrice());
        existingPackage.setPhoto(packageDTO.getPhoto());
        existingPackage.setPiecesCarton(packageDTO.getPiecesCarton());
        Plate plate = plateRepository.findByIdAndDeletedFalse(packageDTO.getPlateId())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
        existingPackage.setPlateId(plate);
        Package updatedPackage = packageRepository.save(existingPackage);
        updatedPackage.setId(id);
        return modelMapper.map(updatedPackage, PackageDTO.class);
    }

    public void deletePackage(Long id) throws ChangeSetPersister.NotFoundException {
        Package package1 = packageRepository.findByIdAndDeletedFalse(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        package1.setDeleted(true);
        packageRepository.save(package1);
    }
}

