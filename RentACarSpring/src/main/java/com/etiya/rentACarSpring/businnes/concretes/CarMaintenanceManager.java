package com.etiya.rentACarSpring.businnes.concretes;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.etiya.rentACarSpring.businnes.abstracts.CarService;
import com.etiya.rentACarSpring.businnes.abstracts.message.LanguageWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.etiya.rentACarSpring.businnes.abstracts.CarMaintenanceService;
import com.etiya.rentACarSpring.businnes.abstracts.RentalService;
import com.etiya.rentACarSpring.businnes.abstracts.constants.Messages;
import com.etiya.rentACarSpring.businnes.dtos.CarMaintenanceSearchListDto;
import com.etiya.rentACarSpring.businnes.request.CarMaintenanceRequest.CreateCarMaintenanceRequest;
import com.etiya.rentACarSpring.businnes.request.CarMaintenanceRequest.DeleteCarMaintenanceRequest;
import com.etiya.rentACarSpring.businnes.request.CarMaintenanceRequest.UpdateCarMaintenanceRequest;
import com.etiya.rentACarSpring.core.utilities.businnessRules.BusinnessRules;
import com.etiya.rentACarSpring.core.utilities.mapping.ModelMapperService;
import com.etiya.rentACarSpring.core.utilities.results.DataResult;
import com.etiya.rentACarSpring.core.utilities.results.ErrorResult;
import com.etiya.rentACarSpring.core.utilities.results.Result;
import com.etiya.rentACarSpring.core.utilities.results.SuccesDataResult;
import com.etiya.rentACarSpring.core.utilities.results.SuccesResult;
import com.etiya.rentACarSpring.dataAccess.abstracts.CarMaintenanceDao;

import com.etiya.rentACarSpring.entities.CarMaintenance;

@Service
public class CarMaintenanceManager implements CarMaintenanceService {

    private CarMaintenanceDao carMaintenanceDao;
    private ModelMapperService modelMapperService;
    private RentalService rentalService;
    private Environment environment;
    private LanguageWordService languageWordService;
    private CarService carService;

    @Autowired
    public CarMaintenanceManager(CarMaintenanceDao carMaintenanceDao, ModelMapperService modelMapperService,
                                 RentalService rentalService, Environment environment,LanguageWordService languageWordService,
                                 CarService carService) {
        super();
        this.carMaintenanceDao = carMaintenanceDao;
        this.modelMapperService = modelMapperService;
        this.rentalService = rentalService;
        this.environment = environment;
        this.languageWordService = languageWordService;
        this.carService = carService;
    }

    @Override
    public DataResult<List<CarMaintenanceSearchListDto>> getAll() {
        List<CarMaintenance> result = this.carMaintenanceDao.findAll();
        List<CarMaintenanceSearchListDto> response = result.stream().map(
                        carMaintenance -> modelMapperService.forDto().map(carMaintenance, CarMaintenanceSearchListDto.class))
                .collect(Collectors.toList());

        return new SuccesDataResult<List<CarMaintenanceSearchListDto>>(response, languageWordService.getByLanguageAndKeyId(Messages.CarMaintenanceListed));
    }

    @Override
    public Result add(CreateCarMaintenanceRequest createCarMaintenanceRequest) {
        Result result = BusinnessRules.run(checkIfCarIsRentedNow(createCarMaintenanceRequest.getCarId()),
                checkIfCarIsAtMaintenance(createCarMaintenanceRequest.getCarId()));
        if (result != null) {
            return result;
        }

        CarMaintenance carMaintenance = modelMapperService.forRequest().map(createCarMaintenanceRequest,
                CarMaintenance.class);
        this.carMaintenanceDao.save(carMaintenance);
        return new SuccesResult(languageWordService.getByLanguageAndKeyId(Messages.CarMaintenanceAdded));
    }

    @Override
    public Result update(UpdateCarMaintenanceRequest updateCarMaintenanceRequest) {
        CarMaintenance carMaintenance=this.carMaintenanceDao.getById(updateCarMaintenanceRequest.getCarMaintenanceId());
        Result result = BusinnessRules.run(checkDate(carMaintenance.getMaintananceDate(),updateCarMaintenanceRequest.getReturnDate()),
                carService.checkCarExistsInGallery(updateCarMaintenanceRequest.getCarId()),
                checkIfCarMaintenanceExists(updateCarMaintenanceRequest.getCarMaintenanceId()));

        if (result != null) {
            return result;
        }
        CarMaintenance updatedCarMaintenance = modelMapperService.forRequest().map(updateCarMaintenanceRequest,
                CarMaintenance.class);

        updatedCarMaintenance.setCar(carMaintenance.getCar());
        updatedCarMaintenance.setMaintananceDate(carMaintenance.getMaintananceDate());
        this.carMaintenanceDao.save(updatedCarMaintenance);
        return new SuccesResult(languageWordService.getByLanguageAndKeyId(Messages.CarMaintenanceUpdated));
    }

    @Override
    public Result delete(DeleteCarMaintenanceRequest deleteCarMaintenanceRequest) {
        Result result = BusinnessRules.run(
                checkIfCarMaintenanceExists(deleteCarMaintenanceRequest.getCarMaintenanceId()));

        if (result != null) {
            return result;
        }
        this.carMaintenanceDao.deleteById(deleteCarMaintenanceRequest.getCarMaintenanceId());
        return new SuccesResult(languageWordService.getByLanguageAndKeyId(Messages.CarMaintenanceDeleted));
    }

    @Override
    public DataResult<CarMaintenance> getById(int carId) {
        return new SuccesDataResult<CarMaintenance>(this.carMaintenanceDao.getById(carId));
    }

    @Override
    public Result checkIfCarIsAtMaintenance(int carId) {
        List<CarMaintenance> result = this.carMaintenanceDao.getByCar_CarId(carId);
        if (result != null) {
            for (CarMaintenance carMaintenances : this.carMaintenanceDao.getByCar_CarId(carId)) {
                if (carMaintenances.getReturnDate() == null) {
                    return new ErrorResult(languageWordService.getByLanguageAndKeyId(Messages.CarIsAlreadyOnMaintenance));
                }
            }
        }
        return new SuccesResult();
    }

    private Result checkIfCarIsRentedNow(int carId) {
        Result isCarReturned = rentalService.checkCarRentalStatus(carId);
        if (!isCarReturned.isSuccess()) {
            return new ErrorResult(languageWordService.getByLanguageAndKeyId(Messages.CarIsOnRent));
        }
        return new SuccesResult();
    }

    private Result checkDate(Date maintananceDate, Date returnDate){
        if (maintananceDate.compareTo(returnDate)<0){
            return new SuccesResult();
        }
        return new ErrorResult(languageWordService.getByLanguageAndKeyId(Messages.InvalidReturnMaintenanceDate));
    }

    private Result checkIfCarMaintenanceExists(int maintenanceId) {
        if (!this.carMaintenanceDao.existsById(maintenanceId)) {
            return new ErrorResult(languageWordService.getByLanguageAndKeyId(Messages.CarMaintenanceNotFound));
        }
        return new SuccesResult();
    }
}
