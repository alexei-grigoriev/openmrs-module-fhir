/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.fhir.api;

import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.valueset.LocationStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir.api.util.FHIRLocationUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class LocationServiceTest extends BaseModuleContextSensitiveTest {

	protected static final String LOC_INITIAL_DATA_XML = "org/openmrs/api/include/LocationServiceTest-initialData.xml";

	public LocationService getService() {
		return Context.getService(LocationService.class);
	}

	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(LOC_INITIAL_DATA_XML);
	}

	@Test
	public void shouldSetupContext() {
		assertNotNull(getService());
	}

	@Test
	public void getLocation_shouldReturnResourceIfExists() {
		String locationUuid = "f08ba64b-ea57-4a41-b33c-9dfc59b0c60a";
		Location fhirLocation = getService().getLocation(locationUuid);
		assertNotNull(fhirLocation);
		assertEquals(fhirLocation.getId().toString(), locationUuid);

	}

	@Test
	public void searchLocationsById_shouldReturnBundle() {
		String locationUuid = "f08ba64b-ea57-4a41-b33c-9dfc59b0c60a";
		List<Location> locations = getService().searchLocationsById(locationUuid);
		assertNotNull(locations);
		assertEquals(1, locations.size());
		assertEquals(locations.get(0).getId().getIdPart(), locationUuid);
	}

	@Test
	public void searchLocationsByName_shouldReturnBundle() {
		String name = "Test Parent Location";
		String locationUuid = "f08ba64b-ea57-4a41-b33c-9dfc59b0c60a";
		List<Location> locations = getService().searchLocationsByName(name);
		assertNotNull(locations);
		assertEquals(1, locations.size());
		assertEquals(locations.get(0).getId().getIdPart(), locationUuid);
	}

	@Test
	public void searchActiveLocations_shouldReturnBundle() {
		List<Location> locations = getService().searchLocationsByStatus(true);
		assertNotNull(locations);
		assertEquals(5, locations.size());
	}
	
	@Test
	public void deleteLocation_shouldDeleteTheSpecifiedLocation()
	{
		org.openmrs.api.LocationService locationService = Context.getLocationService();
		org.openmrs.Location location = locationService.getLocation(6);
		assertNotNull(location);
		String Uuid = location.getUuid();
		getService().deleteLocation(Uuid);
		location = locationService.getLocation(location.getLocationId());
		assertNull(location);
	}

    @Test
    public void updateLocationById_shouldAddResourceIfNotExistsElseUpdateExistingResource() {
        String locationUuid = "f08ba64b-ea57-4a41-b33c-9dfc59b0c60a";
        Location fhirLocation = getService().getLocation(locationUuid);
        org.openmrs.Location omrsLocation = FHIRLocationUtil.generateOpenMRSLocation(fhirLocation, new ArrayList<String>());
        assertNotNull(omrsLocation);
        assertEquals(fhirLocation.getId().toString(), omrsLocation.getUuid().toString());
        assertEquals(fhirLocation.getName(), omrsLocation.getName());
        assertEquals(fhirLocation.getDescription(), omrsLocation.getDescription());
        AddressDt fhirAddress = fhirLocation.getAddress();
        assertEquals(fhirAddress.getCity(), omrsLocation.getCityVillage());
        assertEquals(fhirAddress.getCountry(), omrsLocation.getCountry());
        assertEquals(fhirAddress.getState(), omrsLocation.getStateProvince());
        assertEquals(fhirAddress.getPostalCode(), omrsLocation.getPostalCode());
        for (int i = 0; i < fhirAddress.getLine().size(); i++) {
            switch (i + 1) {
                case 1:
                    assertEquals(fhirAddress.getLine().get(i).toString(), omrsLocation.getAddress1());
                    break;
                case 2:
                    assertEquals(fhirAddress.getLine().get(i).toString(), omrsLocation.getAddress2());
                    break;
                case 3:
                    assertEquals(fhirAddress.getLine().get(i).toString(), omrsLocation.getAddress3());
                    break;
                case 4:
                    assertEquals(fhirAddress.getLine().get(i).toString(), omrsLocation.getAddress4());
                    break;
                case 5:
                    assertEquals(fhirAddress.getLine().get(i).toString(), omrsLocation.getAddress5());
                    break;
            }

        }
        Location.Position position = fhirLocation.getPosition();
        if (position.getLongitude() != null && position.getLatitude() != null) {
            assertEquals(position.getLatitude().toString(), omrsLocation.getLatitude());
            assertEquals(position.getLongitude().toString(), omrsLocation.getLongitude());
        }
        String status = fhirLocation.getStatus();
        if (status.equalsIgnoreCase(LocationStatusEnum.ACTIVE.toString())) {
            assertFalse(omrsLocation.getRetired());
        } else if (status.equals((LocationStatusEnum.INACTIVE.toString()))) {
            // throw error and return error message in response.? OR call locationServcice.retireLocation() instead of locationService.saveLocation()
            assertFalse(omrsLocation.getRetired());
        }
    }

}
