from locust import HttpUser, task, between


class PlantIQUser(HttpUser):
    wait_time = between(0.5, 1.5)

    @task(3)
    def health_check(self):
        """Most frequent — shows load balancing across replicas"""
        self.client.get("/health")

    @task(2)
    def swagger_redirect(self):
        """Test the root redirect"""
        self.client.get("/", allow_redirects=False)

    @task(1)
    def get_swagger_json(self):
        """Test Swagger JSON generation (heavier endpoint)"""
        self.client.get("/swagger/v1/swagger.json")
