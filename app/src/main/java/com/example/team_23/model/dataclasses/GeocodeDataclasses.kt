package com.example.team_23.model.dataclasses

data class GeocodeBase(val plus_code: GeocodePlus_code?, val results: List<GeocodeResult>?, val status: String?)

data class AddressComponents(val long_name: String?, val short_name: String?, val types: List<String>?)

data class GeocodeBounds(val northeast: Northeast?, val southwest: Southwest?)

data class GeocodeGeometry(val bounds: GeocodeBounds?, val location: GeocodeLocation?, val location_type: String?, val viewport: GeocodeViewport?)

data class GeocodeLocation(val lat: Number?, val lng: Number?)

data class GeocodeNortheast(val lat: Number?, val lng: Number?)

data class GeocodePlus_code(val compound_code: String?, val global_code: String?)

data class GeocodeResult(val address_components: List<AddressComponents>?, val formatted_address: String?, val geometry: GeocodeGeometry?, val place_id: String?, val plus_code: GeocodePlus_code?, val types: List<String>?)

data class GeocodeSouthwest(val lat: Number?, val lng: Number?)

data class GeocodeViewport(val northeast: GeocodeNortheast?, val southwest: GeocodeSouthwest?)