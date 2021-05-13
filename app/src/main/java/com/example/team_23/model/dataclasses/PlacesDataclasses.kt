package com.example.team_23.model.dataclasses

data class MainBase(val candidates: List<Candidates>?, val status: String?)

data class Candidates(val formatted_address: String?, val geometry: Geometry?, val name: String?)

data class Geometry(val location: PlaceLocation?, val viewport: Viewport?)

data class PlaceLocation(val lat: Number?, val lng: Number?)

data class North(val lat: Number?, val lng: Number?)

data class South(val lat: Number?, val lng: Number?)

data class Viewport(val northeast: Northeast?, val southwest: Southwest?)