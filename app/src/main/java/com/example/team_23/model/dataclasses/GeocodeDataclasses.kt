package com.example.team_23.model.dataclasses

data class Address_components(val long_name: String?, val short_name: String?, val types: List<String>?)

data class Address_components2(val long_name: String?, val short_name: String?, val types: List<String>?)

data class GeocodeBase(val plus_code: GeocodePlus_code?, val results: List<Any>?, val status: String?)

data class GeocodeBounds(val northeast: Northeast?, val southwest: Southwest?)

data class GeocodeGeometry(val bounds: Bounds?, val location: GeocodeLocation?, val location_type: String?, val viewport: Viewport?)

data class GeocodeLocation(val lat: Number?, val lng: Number?)

data class GeocodeNortheast(val lat: Number?, val lng: Number?)

data class GeocodePlus_code(val compound_code: String?, val global_code: String?)

data class Results1984577908(val address_components: List<Address_components>?, val formatted_address: String?, val geometry: Geometry?, val place_id: String?, val plus_code: GeocodePlus_code?, val types: List<String>?)

data class GeocodeSouthwest(val lat: Number?, val lng: Number?)

data class GeocodeViewport(val northeast: Northeast?, val southwest: Southwest?)