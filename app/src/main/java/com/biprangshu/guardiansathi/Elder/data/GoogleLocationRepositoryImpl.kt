package com.biprangshu.guardiansathi.Elder.data

import android.util.Log
import com.biprangshu.guardiansathi.Elder.presentation.screens.EmergencyNumber
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume


class GoogleLocationRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient
) : GoogleLocationRepository {

//    private val nationalHelplines = listOf(
//        EmergencyNumber("Police",         "100",   isLocal = false),
//        EmergencyNumber("Ambulance",      "108",   isLocal = false),
//        EmergencyNumber("Fire Brigade",   "101",   isLocal = false),
//        EmergencyNumber("Elder Helpline", "14567", isLocal = false),
//        EmergencyNumber("Women Helpline", "1091",  isLocal = false),
//    )

    override suspend fun getNearbyEmergencyNumbers(
        latitude: Double,
        longitude: Double
    ): List<EmergencyNumber> {
        val nearbyPlaces = mutableListOf<EmergencyNumber>()

        val placeTypes = listOf(
            "police",
            "hospital",
            "fire_station",
            "pharmacy"
        )

        placeTypes.forEach { type ->
            try {
                val response = fetchNearbyPlacesOfType(type, latitude, longitude)
                Log.d("EmergencyRepo", "Type: $type → ${response.size} results")
                nearbyPlaces.addAll(response)
            } catch (e: Exception) {
                Log.e("EmergencyRepo", "Failed to fetch $type: ${e.message}")
            }
        }

        Log.d("EmergencyRepo", "Total nearby: ${nearbyPlaces.size}")
        return nearbyPlaces
    }

    override suspend fun fetchNearbyPlacesOfType(
        type: String,
        latitude: Double,
        longitude: Double
    ): List<EmergencyNumber> = suspendCancellableCoroutine { continuation ->

        val request = SearchNearbyRequest.builder(
            CircularBounds.newInstance(
                LatLng(latitude, longitude),
                5000.0
            ),
            listOf(
                Place.Field.DISPLAY_NAME,
                Place.Field.NATIONAL_PHONE_NUMBER,
                Place.Field.FORMATTED_ADDRESS
            )
        )
            .setIncludedTypes(listOf(type))
            .setMaxResultCount(2)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                Log.d("PlacesAPI", "Type: $type — found ${response.places.size} places")

                val results = response.places.mapNotNull { place ->
                    Log.d("PlacesAPI", "  Place: ${place.displayName} | phone: ${place.nationalPhoneNumber}")

                    if (!place.nationalPhoneNumber.isNullOrEmpty()) {
                        EmergencyNumber(
                            type =type,
                            name = place.displayName ?: type,
                            phoneNumber = place.nationalPhoneNumber!!,
                            address = place.formattedAddress ?: "",
                            isLocal = true
                        )
                    } else {
                        Log.d("PlacesAPI", "  Skipped — no phone number")
                        null
                    }
                }
                continuation.resume(results)
            }
            .addOnFailureListener { e ->
                Log.e("PlacesAPI", "SearchNearby failed for $type: ${e.message}")
                continuation.resume(emptyList())
            }

        continuation.invokeOnCancellation {
            Log.d("PlacesAPI", "Cancelled for $type")
        }
    }
}