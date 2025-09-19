import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';
import axios from 'axios';

const hotelServiceHost = process.env.HOTEL_SERVICE_HOST || 'localhost';
const hotelServicePort = process.env.HOTEL_SERVICE_PORT || '8084';
const hotelServiceBaseUrl = `http://${hotelServiceHost}:${hotelServicePort}/api/hotels`

const typeDefs = gql`
  type Hotel @key(fields: "id") {
    id: ID!
    name: String
    city: String
    stars: Int
  }

  type Query {
    hotelsByIds(ids: [ID!]!): [Hotel]
  }
`;

const resolvers = {
  Hotel: {
    __resolveReference: async ({ id }) => {
      try {
        const response = await axios.get(`${hotelServiceBaseUrl}/${id}`);
        return response.data;
      } catch (error) {
        if (error.response && error.response.status === 404) {
          return null;
        }
        console.error(`Error resolving hotel reference with id ${id}:`, error.message);
        return null;
      }
    },
  },
  Query: {
    hotelsByIds: async (_, { ids }) => {
      const hotelPromises = ids.map(id => 
        axios.get(`${hotelServiceBaseUrl}/${id}`)
          .then(response => response.data)
          .catch(error => {
            if (error.response && error.response.status === 404) {
              return null;
            }
            console.error(`Error fetching hotel with id ${id}:`, error.message);
            return null;
          })
      );

      const hotels = await Promise.all(hotelPromises);
      return hotels;
    },
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
});

startStandaloneServer(server, {
  listen: { port: 4002 },
}).then(() => {
  console.log('✅ Hotel subgraph ready at http://localhost:4002/');
});
