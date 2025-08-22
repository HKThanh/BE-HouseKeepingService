package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByCustomer_CustomerId(String customerId);

    Optional<Address> findByCustomer_CustomerIdAndIsDefaultTrue(String customerId);

    List<Address> findByDistrictAndCity(String district, String city);
}