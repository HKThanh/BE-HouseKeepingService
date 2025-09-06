package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByCustomer_CustomerId(String customerId);

    Optional<Address> findByCustomer_CustomerIdAndIsDefaultTrue(String customerId);

    List<Address> findByDistrictAndCity(String district, String city);

    // Find by customer ID
    List<Address> findByCustomer_CustomerIdOrderByCreatedAtDesc(String customerId);
    
    // Find customer's default address
    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByCustomerId(@Param("customerId") String customerId);
    
    
    // Validate customer address ownership
    @Query("SELECT COUNT(a) > 0 FROM Address a WHERE a.addressId = :addressId AND a.customer.customerId = :customerId")
    boolean validateCustomerAddressOwnership(@Param("addressId") String addressId,
                                           @Param("customerId") String customerId);
    
    // Get address with customer info
    @Query("SELECT a FROM Address a " +
           "JOIN FETCH a.customer c " +
           "WHERE a.addressId = :addressId")
    Optional<Address> findAddressWithCustomer(@Param("addressId") String addressId);

        // Find addresses by district and city with customer info
    @Query("SELECT a FROM Address a " +
           "LEFT JOIN FETCH a.customer " +
           "WHERE a.district = :district AND a.city = :city")
    List<Address> findByDistrictAndCityWithCustomer(@Param("district") String district,
                                                   @Param("city") String city);
}